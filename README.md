# Proyecto_Plataformas

## Autores:
- Pedro Caso - 241286
- Hugo Méndez - 241265
- Diego Calderón - 241263

## Descripción:
Este proyecto fue creado con el propósito de ayudar a los estudiantes de la UVG y su STAFF a organizar de mejor manera los espacios recreativos dentro de la universidad. Se tomó como referencia varias opiniones de estudiantes dque han tenido problema de disponibilidad para ocupar estos lugares, ya sea por diversos eventos o bien por otros estudiantes que ya ocuparon el lugar. Para llevar una mejor organización y que todos puedan disponer de un tiempo justo en cada lugar recreativo, se propone una aplicación capaz de ver la disponibilidad de los lugares en tiempo real, capaz de realizar reservas por os estudiantes que deben ser aprobadas por el STAFF y debe ser inclusiva con todos los estudiantes, a esto se refiere, una UI fácil de entender y capaz de soportar diversos idiomas.

### Tecnologías necesarias para la aplicacion:
- Kotlin + Android Studio
- Jetpack Compose para UI declarativa
- Jetpack Navigation Compose para navegación
- Arquitectura MVI con ViewModel y Use Cases
- Firebase Auth + Firestore (servicios externos)
- Room Database para caché offline
- Soporte bilingüe (Español/Inglés)
- Recursos organizados

## Link del repo:
https://github.com/hmndzzl/Proyecto_Plataformas

## link del video demostrativo:
https://youtu.be/jlatOp7VCkc

## diagrama de arquitectura o flujo del código:

com.example.proyecto/
│   │ 
│   │   ├── data
│   │   │   ├── local
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── dao/
│   │   │   │   │   ├── RoomDaos.kt
│   │   │   │   │   ├── DataStore
│   │   │   │   │      └── UserPreferencesDataStore.kt
│   │   │   │   ├── mapper
│   │   │   │   │      └── Mappers.kt
│   │   │   │   │ 
│   │   │   │   ├── Repository
│   │   │   │          └── AuthRepository.kt
│   │   │   │          └── SpaceRepository.kt
│   │   ├── domain
│   │   │     ├── model
│   │   │     │      └── RoomModels.kt
│   │   │     ├── Usecase 
│   │   │     │      └── GetReservationsForMonthUseCase.kt
│   │   │     │      └── UseCase.kt
│   │   ├── Presentation
│   │   │     ├── admin
│   │   │     │      └── AdminScreen.kt
│   │   │     │      └── AdminViewModel.kt
│   │   │     ├── availability
│   │   │     │      └── AvailabilityScreen.kt
│   │   │     │      └── AvailabilityViewModel.kt
│   │   │     ├── Dashboard
│   │   │     │      └── DashboardScreen.kt
│   │   │     │      └── DashboardViewModel.kt
│   │   │     ├── day_reservations
│   │   │     │      └── DayReservationsScreen.kt
│   │   │     ├── login
│   │   │     │      └── LoginScreen.kt
│   │   │     │      └── LoginViewModel.kt
│   │   │     ├── navigation
│   │   │     │      └── Navigation.kt
│   │   │     │      └── ViewModelFactories.kt
│   │   │     ├── Profile
│   │   │     │      └── ProfileScreen.kt
│   │   │     │      └── ProfileViewModel.kt
│   │   │     ├── reserve
│   │   │     │      └── ReserveScreen.kt
│   │   │     │      └── ReserveViewModel.kt
│   │   │     ├── Ui.theme
│   │   │      
│   └── MainActivity.kt   

## En caso de que no se vea bien el diagrama:
<img width="330" height="789" alt="image" src="https://github.com/user-attachments/assets/2c232409-9a2a-44f4-9cf5-db3431cdaf9b" />

