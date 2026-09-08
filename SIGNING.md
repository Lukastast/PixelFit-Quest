# Release signing (Play App Bundle)

PixelFit Quest uploads an Android App Bundle (`.aab`) signed with a local **upload** keystore. The Play Console then re-signs with the app signing key. This repo never stores that keystore or its passwords.

Package / `applicationId` is **`com.pixelfitquest`**. Do not change it; Play and Firebase both key off this name.

## What not to commit

- `pixelfit-upload.jks` (or any `*.jks` / `*.keystore`)
- `storePassword` / `keyPassword` values
- `Android/local.properties` (already gitignored; Android Studio also writes `sdk.dir` here)

Keep the keystore **outside git**. A typical place is the repo parent or a secrets directory, referenced by path.

## Configure signing

`Android/app/build.gradle.kts` reads four values from **environment variables first**, then **`Android/local.properties`**:

| Property | Meaning |
|---|---|
| `storeFile` | Path to the upload keystore (`pixelfit-upload.jks`) |
| `storePassword` | Keystore password |
| `keyAlias` | Key alias (often `upload` or `pixelfit-upload`) |
| `keyPassword` | Key password (may match the store password) |

`storeFile` may be absolute, or relative to `Android/app/`, `Android/`, or the repo root.

Environment aliases also work if your CI only allows uppercase names: `STORE_FILE` / `PIXELFIT_STORE_FILE` / `RELEASE_STORE_FILE` (and the same pattern for `storePassword`, `keyAlias`, `keyPassword`).

### `Android/local.properties` (local machines)

```properties
sdk.dir=/path/to/Android/Sdk

storeFile=/absolute/path/to/pixelfit-upload.jks
storePassword=your-store-password
keyAlias=upload
keyPassword=your-key-password
```

If a password contains `#`, `!`, or `\`, escape it as a Java properties value (or use env vars instead).

### Environment (CI)

```bash
export storeFile=/secrets/pixelfit-upload.jks
export storePassword='...'
export keyAlias=upload
export keyPassword='...'
```

## Build a signed release bundle

From `Android/`:

```bash
./gradlew :app:bundleRelease
```

The signed AAB is:

```
Android/app/build/outputs/bundle/release/app-release.aab
```

Upload that file in Play Console. `bundleRelease` uses the release `signingConfig` **only when all four properties are set**. If none are set, debug/unsigned contributor builds still configure; Play will reject an AAB that is not signed with the upload key.

If some-but-not-all of the four values are present, configuration fails with a list of the missing keys.

## Create an upload keystore (once)

```bash
keytool -genkeypair -v \
  -keystore pixelfit-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Store the JKS and passwords in a password manager. Losing the upload key requires Play support to reset it.

## Debug vs release

- **Debug** (`assembleDebug`) uses the usual Android debug keystore. Workouts, IMU, and the rest of the app are unrelated to this signing setup.
- **Release AAB** for Play must use `pixelfit-upload.jks` via the properties above.
