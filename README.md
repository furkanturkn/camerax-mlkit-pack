<a name="readme-top"></a>


[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<div align="center">
  <h3 align="center">CameraX ML Kit Pack</h3>

  <p align="center">
    An awsome library that contains ML Kit and Camrax implementation! You don't need ZXing anymore.
    <br />
    <a href="https://github.com/furkanturkn/camerax-mlkit-pack/issues">Report Bug</a>
    ·
    <a href="https://github.com/furkanturkn/camerax-mlkit-pack/issues">Request Feature</a>
  </p>
</div>


<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#getting-started">Getting Started</a></li>
    <li><a href="#implementation">Implementation</a></li>
    <li><a href="#CameraxManager-functionalities">CameraxManager functionalities</a></li>
    <li><a href="#Suggestions-for-Reader-Formats">Suggestions for Reader Formats</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project
<div align="center">
<img src="https://user-images.githubusercontent.com/51923824/218333385-f66de0a4-4f4a-413d-9455-ce196e3ec12b.png" width="250" height="600">
</div>

No need to waste time on CameraX and ML Kit app anymore. This library will speed up your development 100%

<b>Features:</b>
* CameraX image capturing
* Ml-Kit barcode scanning with accuracy algorithm.
* Flash, tap to focus, switch back-front camera.
* Stop-start ML Kit barcode reading and camera.
* Run **without** google play services

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* [![Next][kotlinlang.org]][kotlin-url]
* <img src="https://user-images.githubusercontent.com/51923824/218334431-179c83c7-f7d5-4e10-8c50-bb790202c7ef.png" alt="Logo" width="60" height="60">
* CameraX
* Kotlin coroutine
<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Getting Started

1. Add maven-jitpack into project level gradle file.
   ```sh
   repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
   ```
2. Add this into dependencies which is in module level gradle file.
   ```sh
   dependencies {
       ...
       implementation 'com.github.furkanturkn:camerax-mlkit-pack:1.1.2'
   }
   ```
<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Implementation

1. Get camera permission.
2. Init CameraxManager for activity
    ```kotlin
    cameraxManager = CameraxManager.getInstance(
            this,
            null,
            previewView, 
            focusRing, //(ImageView) image that appears with focus animation when clicked on the screen.
            1 //(Int)(optional) start with FRONT[0] or BACK[1] camera. Default = BACK[1].
        )
    ```
   Init CameraxManager for fragment
    ```kotlin
    cameraxManager = CameraxManager.getInstance(
            context,
            this,
            previewView, 
            focusRing, //(ImageView) image that appears with focus animation when clicked on the screen.
            1 //(Int)(optional) start with FRONT[0] or BACK[1] camera. Default = BACK[1].
        )
    ```
2. Destroy references.
    ```kotlin
    override fun onDestroy() {
        super.onDestroy()
        cameraxManager?.destroyReferences()
    }
    ```
<p align="right">(<a href="#readme-top">back to top</a>)</p>

### CameraxManager functionalities

1. How to start camera?
   ```kotlin
    cameraxManager.startCamera()
   ```
2. How to start barcode reading?
   ```kotlin
    cameraxManager?.setReaderFormats(
            ReaderType.FORMAT_QR_CODE.value,
            ReaderType.FORMAT_EAN_8.value,
            ReaderType.FORMAT_EAN_13.value,
            .
            .
            .
        )
    cameraxManager?.startReading()
   ```
   
3. How to stop reading and camera?
   ```kotlin
    cameraxManager?.stopReading()
    cameraxManager?.stopCamera()
   ```

4. How to change flash status?
   ```kotlin
    cameraxManager.changeFlashStatus()
   ```

5. How to change camera type (front-back)?
   ```kotlin
    cameraxManager.changeCameraType()
   ```
6. How to capture photo?
   ```kotlin
    cameraxManager.capturePhoto()
   ```
6. How to change accuracy level of barcode reading?
   ```kotlin
     cameraxManager.setReadingAccuracyLevel(levelOfAccuracy)
   ```
   levelOfAccuracy = <b>1</b> (Less accurate, fastest level. If you select only QR type the accuracy level is automatically set to 1.)<br>
   levelOfAccuracy = <b>2</b> (Minimum requirement for correct barcode reading.)<br>
   levelOfAccuracy = <b>3</b> (<b>Recommended and default</b> accuracy level.)
   
   
<p align="right">(<a href="#readme-top">back to top</a>)</p>

#### Suggestions for Reader Formats

```
For product barcodes:
  ReaderType.FORMAT_EAN_8.value
  ReaderType.FORMAT_EAN_13.value
  ReaderType.FORMAT_UPC_E.value
  ReaderType.FORMAT_UPC_A.value

For QR codes:
  ReaderType.FORMAT_QR_CODE.value
```
<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Callbacks example usage
```kotlin
  cameraxManager?.apply {
      setQrReadSuccessListener { result ->
          println("QR RESULT ----------> $result")
          tvReadResult.text = result
      }

      setFlashStatusChangedListener { status ->
          when (status) {
              FlashStatus.ENABLED -> {
                  btnFlash.setBackgroundResource(R.drawable.baseline_flash_on_24)
              }
              FlashStatus.DISABLED -> {
                  btnFlash.setBackgroundResource(R.drawable.baseline_flash_off_24)
              }
          }
      }

      setPhotoCaptureResultListener { capturedBitmap ->
          runOnUiThread {
              ivCapturePreview.setImageBitmap(capturedBitmap)
          }
      }
  }
```
<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[contributors-shield]: https://img.shields.io/github/contributors/furkanturkn/camerax-mlkit-pack.svg?style=for-the-badge
[contributors-url]: https://github.com/furkanturkn/camerax-mlkit-pack/contributors

[forks-shield]: https://img.shields.io/github/forks/furkanturkn/camerax-mlkit-pack.svg?style=for-the-badge
[forks-url]: https:/github.com/furkanturkn/camerax-mlkit-pack/network/members

[stars-shield]: https://img.shields.io/github/stars/furkanturkn/camerax-mlkit-pack.svg?style=for-the-badge
[stars-url]: https://github.com/furkanturkn/camerax-mlkit-pack/stargazers

[issues-shield]: https://img.shields.io/github/issues/furkanturkn/camerax-mlkit-pack.svg?style=for-the-badge
[issues-url]: https://github.com/furkanturkn/camerax-mlkit-pack/issues

[license-shield]: https://img.shields.io/github/license/furkanturkn/camerax-mlkit-pack.svg?style=for-the-badge
[license-url]: https://github.com/furkanturkn/camerax-mlkit-pack/blob/master/LICENSE.txt

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/furkanturkan

[product-screenshot]: https://user-images.githubusercontent.com/51923824/218333385-f66de0a4-4f4a-413d-9455-ce196e3ec12b.png

[kotlinlang.org]: https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white
[kotlin-url]: https://kotlinlang.org/
