package pl.szczodrzynski.tracker.ui.screen.training

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.data.db.AppDb
import pl.szczodrzynski.tracker.data.entity.TrainingComment
import pl.szczodrzynski.tracker.data.entity.TrainingRun
import pl.szczodrzynski.tracker.data.entity.joins.TrainingFull
import pl.szczodrzynski.tracker.manager.TrackerManager
import pl.szczodrzynski.tracker.service.data.TrackerCommand
import pl.szczodrzynski.tracker.ui.screen.training.metadata.TrainingMetadataManager
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
	val manager: TrackerManager,
	private val appDb: AppDb,
) : ViewModel() {

	sealed interface State {
		data object Loading : State
		data class InProgress(val training: TrainingFull) : State
		data class Finished(val training: TrainingFull) : State
	}

	private val _state = MutableStateFlow<State>(State.Loading)
	val state = _state.asStateFlow()

	private lateinit var training: TrainingFull
	private val metadataManager = TrainingMetadataManager()

	fun loadTraining(id: Int, isHistory: Boolean) = viewModelScope.launch {
		if (state.value !is State.Loading)
			return@launch
		_state.update { State.Loading }
		appDb.trainingDao.getOneFull(id).collect { training ->
			training ?: return@collect
			this@TrainingViewModel.training = training
			if (!isHistory && training.training.id == manager.training.value?.id)
				_state.update { State.InProgress(training) }
			else
				_state.update { State.Finished(training) }
		}
	}

	fun updateTrainingMetadata(
		context: Context,
		onPermissionRequired: (permissions: List<String>) -> Unit,
		onLocationDisabled: (e: ResolvableApiException) -> Unit,
		onProgress: (inProgress: Boolean) -> Unit,
	) = viewModelScope.launch {
		var locationLat = training.training.locationLat
		var locationLon = training.training.locationLon

		// if location is not saved, fetch it and return if not possible
		if (locationLat == null || locationLon == null) {
			onProgress(true)
			if (!metadataManager.checkLocationPermissions(context).containsValue(true)) {
				onPermissionRequired(metadataManager.getLocationPermissions())
				return@launch
			}
			try {
				val location = metadataManager.fetchCurrentLocation(context)
				if (location == null) {
					onProgress(false)
					return@launch
				}
				locationLat = location.latitude
				locationLon = location.longitude
			} catch (e: Exception) {
				if (e is ResolvableApiException)
					onLocationDisabled(e)
				else
					onProgress(false)
				return@launch
			}
		}

		// if location name is not saved, run the Geocoder
		var locality: String? = null
		val locationName = training.training.locationName ?: run {
			onProgress(true)
			try {
				val address = metadataManager.fetchLocationAddress(context, locationLat, locationLon)
				locality = address?.locality
				listOfNotNull(
					address?.thoroughfare,
					address?.featureName,
					address?.locality,
				).joinToString().takeIf { it.isNotEmpty() }
			} catch (e: Exception) {
				null
			}
		}

		// update the title if it's still the default
		val time = training.training.dateTime.atZone(ZoneId.systemDefault())
			.toLocalTime()
			.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
		val defaultTitle = context.getString(R.string.training_default_title, time)
		val title = if (training.training.title != defaultTitle || locality == null)
			training.training.title
		else
			context.getString(R.string.training_located_title, locality)

		val newTraining = training.training.copy(
			title = title,
			locationName = locationName,
			locationLat = locationLat,
			locationLon = locationLon,
		)

		// save training if changed
		if (newTraining != training.training) {
			withContext(Dispatchers.IO) {
				appDb.trainingDao.update(newTraining)
			}
		}
		delay(1000L)
		onProgress(false)
	}

	fun sendCommand(command: TrackerCommand) = viewModelScope.launch {
		manager.sendCommand(command)
	}

	fun saveTitle(value: String) = viewModelScope.launch {
		val newTraining = training.training.copy(title = value)
		appDb.trainingDao.update(newTraining)
	}

	fun saveComment(comment: TrainingComment, value: String) = viewModelScope.launch {
		val newComment = comment.copy(comment = value)
		if (comment.id == 0)
			appDb.trainingCommentDao.insert(newComment)
		else
			appDb.trainingCommentDao.update(newComment)
	}

	fun saveRun(run: TrainingRun) = viewModelScope.launch {
		appDb.trainingRunDao.update(run)
	}
}
