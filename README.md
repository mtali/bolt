![Bolt-Clone-1200x630px](https://github.com/mtali/bolt/blob/main/docs/cover.png)

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://getstream.io"><img src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/HayesGordon/e7f3c4587859c17f3e593fd3ff5b13f4/raw/11d9d9385c9f34374ede25f6471dc743b977a914/badge.json" alt="Stream Feeds"></a>
</p>

# 🚗 Bolt Clone App

Welcome to the unofficial Bolt clone app! This project is an early-stage clone of the Bolt app, showcasing modern Android development tools and libraries.

## Status: 🚧 Active Development 🚧

## Features

- **Kotlin**: Modern, expressive programming language.
- **Jetpack Compose**: Declarative UI toolkit.
- **Firebase Auth**: Secure authentication.
- **Stream IO**: Real-time messaging.
- **Google Maps**: Interactive maps.
- **Google Places**: Location search and details.
- **Material3**: Latest Material Design components.
- **Firestore**: Scalable NoSQL database.
- **Coroutines**: Simplified concurrency.

## 📷 Previews
### Driver

<p align="center">
<img src="docs/driver01.png" alt="drawing" width="270" />
<img src="docs/driver02.png" alt="drawing" width="270" />
<img src="docs/driver03.png" alt="drawing" width="270" />
<img src="docs/driver04.png" alt="drawing" width="270" />
<img src="docs/driver05.png" alt="drawing" width="270" />
</p>

### Passenger

<p align="center">
<img src="docs/passenger01.png" alt="drawing" width="270" />
<img src="docs/passenger02.png" alt="drawing" width="270" />
<img src="docs/passenger03.png" alt="drawing" width="270" />
<img src="docs/passenger04.png" alt="drawing" width="270" />
<img src="docs/passenger05.png" alt="drawing" width="270" />
</p>

## Installation

1. **Clone the repo**
   ```sh
   git clone https://github.com/mtali/bolt.git
   cd bolt
   ```
2. **Open in Android Studio and sync the project**
3. **For Firebase configuration, follow [this link](https://firebase.google.com/docs/android/setup)**
4. **Be sure to generate and set the SHA-1**
   ```sh
   ./gradlew signingReport
   ```
5. **Enable the Email/Password sign-in provider** (as shown below)

   ![Enable Email/Password](docs/sign-in-provider.png)
6. **Configure Stream**
   
   Create new project on [getstream](https://getstream.io/dashboard/) and make sure under roles and permission 'user' can update his/her own role

7. **Auth Keys**

   At the root of the project, create a file named `secrets.properties` and add the map and stream API keys. You can find an example in `secrets.defaults.properties`.
   
9. **Run the project**

## License

Distributed under the MIT License. See `LICENSE` for more info.

## Acknowledgements

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Stream IO](https://getstream.io/)
- [Google Maps](https://developers.google.com/maps)
- [Google Places](https://developers.google.com/places)
- [Material Design](https://material.io/)
- [Firestore](https://firebase.google.com/docs/firestore)

## Contact

Emmanuel S Mtali - [@emmanuel_mtali](https://twitter.com/emmanuel_mtali) - emmanuel.mtali@protonmail.com

Project Link: [https://github.com/mtali/bolt](https://github.com/mtali/bolt)

⭐️ If you find this project useful! 🚀

---

Happy coding! 🎉
