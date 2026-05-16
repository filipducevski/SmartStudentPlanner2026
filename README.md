# 📚 Smart Student Planner — Android App

Мобилна апликација за студенти за управување со предмети, задачи и испити.

---

## 🏗️ Архитектура

```
MVVM + Repository Pattern + Clean Architecture
├── UI Layer          (Activities / Fragments / Adapters)
├── ViewModel Layer   (Hilt-injected ViewModels)
├── Repository Layer  (TaskRepository, UserPreferencesRepository)
├── Data Layer
│   ├── Local  → Room Database (tasks, subjects, exams)
│   └── Remote → Firebase Firestore (cloud sync)
└── Service Layer     (FCMService, NotificationScheduler, SyncWorker)
```

---

## 📁 Структура на проектот

```
app/src/main/java/com/smartstudent/planner/
├── SmartStudentApp.kt               ← Application class, notification channels
├── di/
│   └── AppModule.kt                 ← Hilt DI module
├── data/
│   ├── local/
│   │   ├── StudentPlannerDatabase.kt ← Room database
│   │   ├── dao/                     ← TaskDao, SubjectDao, ExamDao
│   │   └── entities/                ← TaskEntity, SubjectEntity, ExamEntity
│   ├── remote/
│   │   └── FirestoreRepository.kt   ← Firebase Firestore CRUD
│   └── repository/
│       ├── TaskRepository.kt        ← Единствен извор (Room + Firestore)
│       └── UserPreferencesRepository.kt ← DataStore settings
├── ui/
│   ├── auth/    SplashActivity, AuthActivity, LoginFragment, RegisterFragment
│   ├── dashboard/ MainActivity, DashboardFragment
│   ├── tasks/   TasksFragment, TaskDetailActivity, TaskAdapter
│   ├── subjects/ SubjectsFragment, SubjectDetailActivity, SubjectAdapter
│   ├── exams/   ExamsFragment, ExamDetailActivity, ExamAdapter
│   └── profile/ ProfileFragment
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── TaskViewModel.kt
│   └── SubjectExamViewModel.kt  (SubjectViewModel + ExamViewModel)
├── service/
│   ├── FCMService.kt            ← Firebase Cloud Messaging
│   ├── NotificationScheduler.kt ← AlarmManager reminders
│   ├── SyncWorker.kt            ← WorkManager background sync
│   └── BootReceiver.kt          ← Re-schedules alarms after reboot
└── util/
    ├── UiState.kt               ← Sealed class: Idle/Loading/Success/Error
    ├── AnalyticsHelper.kt       ← Firebase Analytics wrapper
    ├── DateTimeUtil.kt          ← Date/time helpers
    └── Extensions.kt            ← Kotlin extension functions
```

---

## 🔥 Firebase модули

| Модул              | Употреба                                                    |
|--------------------|-------------------------------------------------------------|
| **Authentication** | Email/Password, Google, Facebook, Anonymous sign-in         |
| **Firestore**      | Cloud sync на tasks / subjects / exams                      |
| **Messaging (FCM)**| Push нотификации за рокови и испити                         |
| **Analytics**      | Screen tracking, task/exam events, sync events, login/signup|

---

## 📱 Поддржани екрани

| Конфигурација         | Ресурс директориум         | Опис                              |
|-----------------------|----------------------------|-----------------------------------|
| Portrait (телефон)    | `res/layout/`              | Стандарден еднокоонен layout      |
| Landscape (телефон)   | `res/layout-land/`         | Две колони (статистики + листа)   |
| Таблет (≥600dp)       | `res/layout-sw600dp/`      | Две панели (overview + задачи)    |

---

## 🌐 Интернационализација

| Јазик       | Директориум       |
|-------------|-------------------|
| Англиски    | `res/values/`     |
| Македонски  | `res/values-mk/`  |

---

## ⚙️ Поставување на проектот

### 1. Клонирај го проектот
```bash
git clone <repo-url>
cd SmartStudentPlanner
```

### 2. Firebase конфигурација
1. Оди на https://console.firebase.google.com
2. Создај нов проект: **SmartStudentPlanner**
3. Додај Android апликација со package: `com.smartstudent.planner`
4. Преземи `google-services.json` и замени го постоечкиот во `/app/`
5. Овозможи ги следните сервиси:
   - **Authentication** → Email/Password, Google Sign-In, Anonymous
   - **Firestore Database** → Create in production mode
   - **Cloud Messaging** → (автоматски овозможено)
   - **Analytics** → (автоматски овозможено)

### 3. Google Sign-In
- Во Firebase Console → Authentication → Google → копирај го `Web client ID`
- Постави го во `res/values/strings.xml` (или `google-services.json` автоматски)

### 4. Facebook Login
1. Оди на https://developers.facebook.com
2. Создај нова апликација → Android
3. Копирај ги вредностите во `res/values/strings.xml`:
   ```xml
   <string name="facebook_app_id">YOUR_APP_ID</string>
   <string name="facebook_client_token">YOUR_CLIENT_TOKEN</string>
   <string name="fb_login_protocol_scheme">fbYOUR_APP_ID</string>
   ```

### 5. Фонтови (опционално)
- Преземи **Nunito** од fonts.google.com
- Постави ги во `res/font/`:
  - `nunito.ttf`
  - `nunito_bold.ttf`
  - `nunito_extrabold.ttf`
- Апликацијата функционира и без нив (системски sans-serif)

### 6. Синхронизирај и стартувај
```bash
./gradlew assembleDebug
```

---

## 🔔 Нотификации

- **Локални** (AlarmManager): task/exam потсетници пред рокот
- **Push** (FCM): серверски нотификации за сите корисници
- **WorkManager**: периодична sync на секои 15 минути (само со интернет)

---

## 🗄️ База на податоци (Room)

Три табели:

| Табела     | Entity          | Клучни колони                                    |
|------------|-----------------|--------------------------------------------------|
| `tasks`    | TaskEntity      | id, userId, title, dueDate, priority, isCompleted|
| `subjects` | SubjectEntity   | id, userId, name, code, professor, color         |
| `exams`    | ExamEntity      | id, userId, title, examDate, subjectId, examType |

---

## 🧪 Тестови

```bash
# Unit тестови
./gradlew test

# Instrumented тестови (Room)
./gradlew connectedAndroidTest
```

---

## 📋 Checklist на барањата

- [x] Мултивју апликација (5 Fragments + 3 Activities)
- [x] Portrait layout (`res/layout/`)
- [x] Landscape layout (`res/layout-land/`)
- [x] Tablet layout (`res/layout-sw600dp/`)
- [x] Македонски јазик (`res/values-mk/strings.xml`)
- [x] Англиски јазик (`res/values/strings.xml`)
- [x] Room база (TaskDao, SubjectDao, ExamDao)
- [x] Firebase Authentication (Email, Google, Facebook, Anonymous)
- [x] Firebase Firestore (cloud sync)
- [x] Firebase Messaging (FCM push + local AlarmManager)
- [x] Firebase Analytics (screen views, events, user properties)
