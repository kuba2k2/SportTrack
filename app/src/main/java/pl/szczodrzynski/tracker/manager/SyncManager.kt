package pl.szczodrzynski.tracker.manager

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.Training
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.TrainingRunSplit
import pl.szczodrzynski.tracker.data.entity.TrainingWeather
import pl.szczodrzynski.tracker.data.entity.joins.TrainingFull
import pl.szczodrzynski.tracker.data.entity.serializer.fromStringMap
import pl.szczodrzynski.tracker.data.entity.serializer.toMap
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SyncManager @Inject constructor(
	private val appDb: AppDb,
) : CoroutineScope {

	override val coroutineContext = Job() + Dispatchers.IO

	private val auth = Firebase.auth
	private val db = Firebase.firestore

	sealed interface State {
		data object Idle : State
		data object Uploading : State
		data object Downloading : State
		data object UploadSuccess : State
		data object DownloadSuccess : State
		data class Error(val e: Exception) : State
	}

	private val _state = MutableStateFlow<State>(State.Idle)
	val state = _state.asStateFlow()

	private suspend fun <T> Task<T>.awaitResult() = suspendCoroutine<T> { continuation ->
		this.addOnCompleteListener {
			if (it.isSuccessful)
				continuation.resume(it.result)
			else
				continuation.resumeWithException(it.exception ?: RuntimeException())
		}
	}

	fun startSyncUpload() = launch {
		val user = auth.currentUser ?: return@launch
		_state.update { State.Uploading }

		try {
			val userRef = db.collection("users")
				.document(user.uid)
			val trainingsRef = userRef.collection("trainings")

			val trainings = appDb.trainingDao.getAllFullNow()
			for (training in trainings) {
				uploadTraining(trainingsRef, training)
			}

			_state.update { State.UploadSuccess }
		} catch (e: Exception) {
			Timber.e(e, "Upload sync exception")
			_state.update { State.Error(e) }
		}
	}

	fun startSyncDownload() = launch {
		val user = auth.currentUser ?: return@launch
		_state.update { State.Downloading }

		try {
			val userRef = db.collection("users")
				.document(user.uid)
			val trainingsRef = userRef.collection("trainings")

			val trainingRefs = trainingsRef.get().awaitResult()
			for (trainingRef in trainingRefs) {
				downloadTraining(trainingRef)
			}

			_state.update { State.DownloadSuccess }
		} catch (e: Exception) {
			Timber.e(e, "Download sync exception")
			_state.update { State.Error(e) }
		}
	}

	private suspend fun uploadTraining(trainingsRef: CollectionReference, trainingFull: TrainingFull) {
		val training = trainingFull.training
		val runList = trainingFull.runList
		val commentList = trainingFull.commentList
		val weatherList = trainingFull.weatherList

		Timber.d("Uploading training $training")
		val trainingRef = trainingsRef.document("${training.id}")
		trainingRef.set(training.toMap()).awaitResult()

		val runsRef = trainingRef.collection("runs")
		for (runFull in runList) {
			val run = runFull.run
			val splits = runFull.splits ?: continue

			Timber.d("Uploading run $run")
			val runRef = runsRef.document("${run.id}")
			runRef.set(run.toMap()).awaitResult()

			val splitsRef = runRef.collection("splits")
			for (split in splits) {
				Timber.d("Uploading split $split")
				val splitRef = splitsRef.document("${split.id}")
				splitRef.set(split.toMap()).awaitResult()
			}
		}

		val commentsRef = trainingRef.collection("comments")
		for (comment in commentList) {
			Timber.d("Uploading comment $comment")
			val commentRef = commentsRef.document("${comment.id}")
			commentRef.set(comment.toMap()).awaitResult()
		}

		val weathersRef = trainingRef.collection("weather")
		for (weather in weatherList) {
			Timber.d("Uploading weather $weather")
			val weatherRef = weathersRef.document("${weather.id}")
			weatherRef.set(weather.toMap()).awaitResult()
		}
	}

	private suspend fun downloadTraining(trainingRef: QueryDocumentSnapshot) {
		Timber.d("Downloading training $trainingRef")
		val training = trainingRef.data.fromStringMap<Training>()
		appDb.trainingDao.insert(training)

		val runRefs = trainingRef.reference.collection("runs").get().awaitResult()
		for (runRef in runRefs) {
			Timber.d("Downloading run $runRef")
			val run = runRef.data.fromStringMap<TrainingRun>()
			appDb.trainingRunDao.insert(run)

			val splitRefs = runRef.reference.collection("splits").get().awaitResult()
			for (splitRef in splitRefs) {
				Timber.d("Downloading split $splitRef")
				val split = splitRef.data.fromStringMap<TrainingRunSplit>()
				appDb.trainingRunSplitDao.insert(split)
			}
		}

		val commentRefs = trainingRef.reference.collection("comments").get().awaitResult()
		for (commentRef in commentRefs) {
			Timber.d("Downloading comment $commentRef")
			val comment = commentRef.data.fromStringMap<TrainingComment>()
			appDb.trainingCommentDao.insert(comment)
		}

		val weatherRefs = trainingRef.reference.collection("weather").get().awaitResult()
		for (weatherRef in weatherRefs) {
			Timber.d("Downloading weather $weatherRef")
			val weather = weatherRef.data.fromStringMap<TrainingWeather>()
			appDb.trainingWeatherDao.insert(weather)
		}
	}
}
