plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.ksp)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.google.gms)
}

android {
	namespace = "pl.szczodrzynski.tracker"
	compileSdk = 35

	defaultConfig {
		applicationId = "pl.szczodrzynski.tracker"
		minSdk = 23
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.credentials)
	implementation(libs.androidx.credentials.play)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.hilt.android)
	implementation(libs.androidx.hilt.navigation.compose)
	implementation(platform(libs.google.firebase))
	implementation(libs.google.firebase.auth)
	implementation(libs.google.identity.googleid)
	implementation(libs.iconics.compose)
	implementation(libs.iconics.typeface.cmd)
	implementation(libs.timber)
	ksp(libs.hilt.android.compiler)
	debugImplementation(libs.androidx.ui.tooling)
}
