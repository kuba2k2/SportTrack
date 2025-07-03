package pl.szczodrzynski.tracker.ui.screen.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import pl.szczodrzynski.tracker.R
import pl.szczodrzynski.tracker.manager.SyncManager
import pl.szczodrzynski.tracker.ui.components.Iconics
import pl.szczodrzynski.tracker.ui.theme.SportTrackTheme

@Composable
@Preview
private fun Preview() {
	SportTrackTheme {
		AccountInfo(
			name = "User Name",
			email = "user@example.com",
			syncState = SyncManager.State.Idle,
		)
	}
}

@Composable
@Preview
private fun PreviewSyncing() {
	SportTrackTheme {
		AccountInfo(
			name = "User Name",
			email = "user@example.com",
			syncState = SyncManager.State.Uploading,
		)
	}
}

@Composable
@Preview
private fun PreviewSuccess() {
	SportTrackTheme {
		AccountInfo(
			name = "User Name",
			email = "user@example.com",
			syncState = SyncManager.State.UploadSuccess,
		)
	}
}

@Composable
@Preview
private fun PreviewError() {
	SportTrackTheme {
		AccountInfo(
			name = "User Name",
			email = "user@example.com",
			syncState = SyncManager.State.Error(RuntimeException()),
		)
	}
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun AccountInfo(
	name: String,
	email: String,
	modifier: Modifier = Modifier,
	syncState: SyncManager.State = SyncManager.State.Idle,
	onUploadSync: () -> Unit = {},
	onDownloadSync: () -> Unit = {},
) {
	Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = stringResource(R.string.login_success_title, name),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = email,
			style = MaterialTheme.typography.titleSmall,
		)

		if (syncState is SyncManager.State.Error) {
			Row(
				modifier = Modifier.padding(top = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Iconics(CommunityMaterial.Icon.cmd_alert_outline)
				Text(
					stringResource(R.string.login_sync_error),
					modifier = Modifier.padding(start = 8.dp),
				)
			}
		}

		when (syncState) {
			SyncManager.State.Uploading, SyncManager.State.Downloading -> {
				Row(
					modifier = Modifier.padding(top = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					CircularProgressIndicator()
					Text(
						stringResource(R.string.login_sync_progress),
						modifier = Modifier.padding(start = 8.dp),
					)
				}
			}

			else -> {
				FlowRow(
					modifier = Modifier.padding(top = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(
						ButtonGroupDefaults.ConnectedSpaceBetween,
						alignment = Alignment.CenterHorizontally,
					),
				) {
					FilledTonalButton(
						onClick = onUploadSync,
						shapes = ButtonShapes(
							ButtonGroupDefaults.connectedLeadingButtonShape,
							ButtonGroupDefaults.connectedLeadingButtonPressShape,
						),
						enabled = syncState != SyncManager.State.Uploading && syncState != SyncManager.State.UploadSuccess,
					) {
						Iconics(CommunityMaterial.Icon.cmd_cloud_upload_outline, size = ButtonDefaults.IconSize)
						Spacer(Modifier.size(ButtonDefaults.IconSpacing))
						Text(
							stringResource(
								if (syncState == SyncManager.State.UploadSuccess)
									R.string.login_sync_success
								else
									R.string.login_sync_upload_button
							)
						)
					}
					FilledTonalButton(
						onClick = onDownloadSync,
						shapes = ButtonShapes(
							ButtonGroupDefaults.connectedTrailingButtonShape,
							ButtonGroupDefaults.connectedTrailingButtonPressShape,
						),
						enabled = syncState != SyncManager.State.Downloading && syncState != SyncManager.State.DownloadSuccess,
					) {
						Iconics(CommunityMaterial.Icon.cmd_cloud_download_outline, size = ButtonDefaults.IconSize)
						Spacer(Modifier.size(ButtonDefaults.IconSpacing))
						Text(
							stringResource(
								if (syncState == SyncManager.State.DownloadSuccess)
									R.string.login_sync_success
								else
									R.string.login_sync_download_button
							)
						)
					}
				}
			}
		}
	}
}
