# رقم مباشر (Direct Number)

تطبيق أندرويد أصلي (Kotlin + Jetpack Compose) لإدخال أرقام الهواتف، تحويلها إلى الصيغة
الدولية الصحيحة عبر مكتبة Google `libphonenumber`، ثم تنفيذ إجراءات مباشرة عليها: فتح
محادثة WhatsApp أو WhatsApp Business، حفظ الرقم في جهات اتصال الجهاز، أو مشاركته كبطاقة
vCard. **كل شيء يعمل محليًا على الجهاز — لا خادم، لا حساب، لا اتصال إنترنت مطلوب.**

> هذا المشروع أُنشئ بالكامل داخل مجلد `android-app/` من هذا المستودع، إلى جانب مشروع ويب
> سابق غير متعلق بهذه المهمة كان موجودًا في جذر المستودع. لم يُحذف أي من الملفات القديمة.

---

## ⚠️ قيد تقني واحد يجب معرفته قبل البدء

بيئة التطوير التي بُني بها هذا المشروع (Claude Code sandbox) **لا تملك اتصالاً بخوادم
`dl.google.com` / `maven.google.com`** (تُحظر على مستوى الشبكة)، وبالتالي:

- لم يكن بالإمكان تشغيل `./gradlew build` فعليًا هنا، لأن Android Gradle Plugin وأدوات
  Android SDK (aapt2، compileSdk 35...) تُجلب حصرًا من مستودعات Google.
- ملف `gradle/wrapper/gradle-wrapper.jar` **غير موجود** (ثنائي Gradle نفسه غير قابل
  للتنزيل من نفس السبب)، لذلك عليك توليده بنفسك في أول مرة تفتح فيها المشروع (خطوة واحدة،
  انظر أدناه).
- في المقابل، **مكتبة `libphonenumber` نفسها متوفرة على Maven Central** (غير محظور)، وتم
  تنزيلها فعليًا واستخدامها للتحقق من كل رقم هاتف مذكور في هذا الملف وفي ملفات الاختبار —
  القيم (E.164، الصيغة الدولية، نوع الرقم) مأخوذة من تشغيل حقيقي للمكتبة، وليست تخمينًا.

باستثناء هذه النقطة، الكود الكامل جاهز، ولا يعتمد على أي بيانات وهمية أو TODO منقوصة.

### توليد Gradle Wrapper (مرة واحدة فقط)

من أي جهاز لديه اتصال إنترنت عادي (جهازك الشخصي، وليس هذه البيئة المعزولة):

```bash
cd android-app
gradle wrapper --gradle-version 8.7 --distribution-type bin
```

أو ببساطة افتح المجلد في **Android Studio** مباشرة — سيكتشف غياب الـ wrapper ويعرض عليك
تنزيله تلقائيًا. أي من الطريقتين كافٍ، ثم يعمل المشروع بشكل طبيعي تمامًا بعدها.

---

## التشغيل والبناء

المتطلبات:
- Android Studio (أحدث إصدار مستقر، Ladybug أو أحدث).
- JDK 17 (مضمّن مع Android Studio الحديث).
- Android SDK: `compileSdk 35`, `minSdk 26` (Android 8.0+), `targetSdk 35`.

الخطوات:
1. افتح مجلد `android-app/` مباشرة في Android Studio (**File → Open**، اختر هذا المجلد
   وليس جذر المستودع).
2. انتظر Gradle Sync (سيولّد الـ wrapper تلقائيًا إذا لم يكن موجودًا، أو استخدم الأمر أعلاه).
3. شغّل التطبيق على محاكي أو جهاز فعلي عبر زر Run ▶️.

للبناء من الطرفية بعد توليد الـ wrapper:
```bash
./gradlew assembleDebug      # بناء APK تجريبي
./gradlew test               # اختبارات الوحدة (JVM، لا تحتاج جهازًا)
./gradlew connectedAndroidTest  # اختبارات الواجهة (تحتاج جهازًا/محاكيًا متصلاً)
```

---

## المكتبات المستخدمة

| المكتبة | الغرض |
|---|---|
| Jetpack Compose (BOM 2024.06.00) + Material 3 (1.3.0) | واجهة المستخدم كاملة |
| Navigation Compose 2.7.7 | التنقل بين الشاشة الرئيسية والإعدادات |
| Jetpack DataStore (Preferences) 1.1.1 | تخزين إعدادات المستخدم محليًا |
| `com.googlecode.libphonenumber:libphonenumber` 8.13.43 | تحليل، تحقق، وتنسيق أرقام الهواتف — المصدر الوحيد للحقيقة في معالجة الأرقام |
| AndroidX Lifecycle (ViewModel + StateFlow) 2.8.4 | إدارة الحالة بنمط MVVM |
| Kotlin Coroutines | العمليات غير المتزامنة (قراءة DataStore، إلخ) |
| AndroidX Core SplashScreen 1.0.1 | شاشة بدء حديثة متوافقة مع Android 12+ |
| AndroidX AppCompat 1.7.0 | فقط لاستخدام `AppCompatDelegate.setApplicationLocales` لتبديل لغة التطبيق يدويًا |
| AndroidX Core FileProvider | مشاركة ملفات VCF عبر `content://` آمن |
| JUnit4 + kotlinx-coroutines-test + Turbine | اختبارات الوحدة |
| Espresso + Compose UI Test | اختبارات الواجهة (Instrumented) |

لا توجد أي مكتبة تحليلات، تتبع، أو اتصال شبكي.

---

## البنية المعمارية (MVVM + طبقات نظيفة)

```
app/src/main/java/com/directnumber/app/
├─ data/
│   ├─ preferences/   → DataStore instance (SettingsDataStore.kt)
│   ├─ model/         → AppSettings, WhatsAppTarget, ThemeMode, AppLanguageOption
│   └─ repository/    → SettingsRepository, RecentsRepository, CountryRepository
├─ domain/
│   ├─ model/         → Country, PhoneNumberResult, ValidationStatus, NumberType
│   ├─ usecase/       → ValidatePhoneNumberUseCase, BuildWhatsAppLinkUseCase, GenerateVCardUseCase
│   └─ validator/     → PhoneNumberValidator (الغلاف الوحيد فوق libphonenumber)
├─ ui/
│   ├─ home/          → HomeScreen, HomeViewModel, HomeUiState
│   ├─ settings/      → SettingsScreen, SettingsViewModel
│   ├─ components/    → كل مكوّنات Compose القابلة لإعادة الاستخدام (17 مكوّنًا)
│   ├─ theme/         → Color / Type / Shape / Theme (Material 3، فاتح وداكن)
│   └─ navigation/    → Routes + NavGraph (Navigation Compose)
├─ util/              → WhatsAppLauncher, ContactLauncher, VCardGenerator,
│                        ClipboardHelper, AppPackageChecker, PhoneNumberFormatter
├─ DirectNumberApp.kt → Application + محدّد خدمات (service locator) يدوي بسيط
└─ MainActivity.kt    → نقطة الدخول، Splash Screen، Edge-to-Edge، تطبيق اللغة والمظهر
```

**الفصل بين الطبقات:**
- `domain/` لا يعرف شيئًا عن Android (باستثناء استخدام `libphonenumber` نفسها) — قابل
  للاختبار بالكامل على JVM عادي دون محاكي.
- `util/` هو الطبقة الوحيدة التي تتحدث مع Android Intents/ContactsContract/FileProvider.
- `ui/` لا تحتوي على أي منطق تحقق من الأرقام — كل ذلك يمر عبر `HomeViewModel` الذي يستدعي
  حالات الاستخدام (`use cases`) في `domain/`.
- لا يوجد إطار حقن تبعيات (Hilt/Koin) — تم تعمّد ذلك: عدد التبعيات صغير بما يكفي ليكون
  محدد خدمات يدوي في `DirectNumberApp.kt` أبسط وأكثر أمانًا (لا "سحر" في وقت التشغيل).

---

## معالجة الأرقام (كيف تعمل)

1. أثناء الكتابة، `util/PhoneNumberFormatter.sanitizeInput()` يُبقي فقط على: الأرقام،
   علامة `+` واحدة في البداية، المسافات، الشرطات، والأقواس — أي حرف آخر (بما فيه الأحرف
   الأبجدية من نص مُلصَق من الحافظة) يُحذف فورًا وبصمت.
2. عند كل تغيير، `domain/usecase/ValidatePhoneNumberUseCase` يستدعي
   `domain/validator/PhoneNumberValidator` (الغلاف الوحيد فوق `PhoneNumberUtil` من
   libphonenumber) مع الرقم المُدخل والدولة المختارة كدولة افتراضية للتحليل.
3. **لا توجد أي قاعدة يدوية مكتوبة بخط اليد لأي دولة، بما فيها الأرجنتين.** كل تحويل
   (حذف `0`/`15` المحليين، إضافة `9` بعد رمز الدولة للأرقام المحمولة الأرجنتينية، إلخ)
   يتم بالكامل عبر بيانات خطة الترقيم الرسمية داخل المكتبة.
4. الناتج (`PhoneNumberResult`) يحدد إحدى 4 حالات تحقق مطابقة تمامًا لما طُلب:
   - **صالح** (`VALID`): رقم مطابق تمامًا لخطة الترقيم.
   - **غير مكتمل** (`INCOMPLETE`): عدد أرقام غير كافٍ حتى الآن.
   - **غير صالح** (`INVALID`): طول معقول لكنه لا يطابق أي نمط صحيح.
   - **يحتاج إلى مراجعة** (`NEEDS_REVIEW`): الرقم يبدأ بـ `+` ورمز دولي مختلف عن الدولة
     المختارة يدويًا — تظهر بطاقة تعارض بزرين ("استخدام الدولة المكتشفة" /
     "الاستمرار بالدولة المختارة") تمامًا كما هو مطلوب.
5. عند الصلاحية، تُشتق ثلاث صيغ جاهزة: `E.164` (`+5491123456789`)، صيغة دولية مقروءة
   (`+54 9 11 2345-6789`)، ورقم WhatsApp (بدون `+`، `5491123456789`).

**كل هذه القيم مُتحقَّق منها فعليًا** عبر تشغيل مباشر لمكتبة libphonenumber الحقيقية
(الإصدار 8.13.43 من Maven Central) أثناء كتابة اختبارات الوحدة — وليست قيمًا مفترضة.

---

## فتح WhatsApp العادي وWhatsApp Business

- `util/WhatsAppLauncher.kt` يبني `Intent(ACTION_VIEW)` على رابط
  `https://wa.me/<الرقم_بدون_+>?text=<رسالة_مرمّزة>` مع `setPackage(...)` **صريح**
  (`com.whatsapp` أو `com.whatsapp.w4b`) — لا يُترك الأمر لنظام اختيار التطبيقات، فيُضمن
  فتح التطبيق المطلوب تحديدًا.
- `AndroidManifest.xml` يعلن `<queries>` لكلا الحزمتين، متوافقًا مع قيود ظهور الحزم
  (Package Visibility) منذ Android 11.
- قبل الفتح، يُتحقق من التثبيت عبر `util/AppPackageChecker.kt`. إن لم يكن التطبيق
  المطلوب مثبتًا، يظهر `AppNotInstalledDialog` يعرض: فتح التطبيق البديل (إن كان مثبتًا)،
  فتح متجر Google Play، أو الإلغاء — **دون أي انتقال تلقائي** لتطبيق آخر.
- الرسالة الجاهزة (إن وُجدت) تُدرَج داخل رابط `wa.me` بعد ترميزها بـ UTF-8 percent-encoding
  (وليس بصيغة `application/x-www-form-urlencoded` الخام) — **لا تُرسَل الرسالة تلقائيًا
  أبدًا**، تُفتح المحادثة والرسالة مكتوبة فقط بانتظار ضغط المستخدم لزر الإرسال.
- خيار "الدولة الافتراضية / اسألني في كل مرة" موجود في الإعدادات ومطبَّق بالكامل في
  `HomeViewModel.onOpenWhatsAppClicked`؛ الزرّان الظاهران في الشاشة الرئيسية ("فتح في
  WhatsApp" و"فتح في WhatsApp Business") يبقيان محددَين وفوريين (نقرة واحدة) لتقليل عدد
  اللمسات، بينما آلية "اسألني في كل مرة" و`WhatsAppPickerDialog` (Bottom Sheet مع خيار
  "تذكّر اختياري") مطبَّقة وقابلة للاستدعاء عبر نفس مسار `HomeViewModel` عند تمرير هدف
  غير محدد.

---

## حفظ جهة الاتصال

`util/ContactLauncher.kt` يستخدم حصرًا:
```kotlin
Intent(ContactsContract.Intents.Insert.ACTION).apply {
    type = ContactsContract.RawContacts.CONTENT_TYPE
    putExtra(ContactsContract.Intents.Insert.NAME, name)
    putExtra(ContactsContract.Intents.Insert.PHONE, e164Number)
    putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, TYPE_MOBILE)
}
```
هذا يفتح **شاشة النظام الأصلية** لإنشاء جهة اتصال (تطبيق جهات الاتصال المثبت على الجهاز)،
حيث يختار المستخدم بنفسه الحساب (Google، Samsung، الهاتف، إلخ) ويؤكد الحفظ بعينيه. **لا
تُطلب صلاحية `READ_CONTACTS` ولا `WRITE_CONTACTS`** — لا حاجة لها لأن الحفظ الفعلي يتم
داخل تطبيق النظام، لا داخل هذا التطبيق.

---

## إنشاء ومشاركة بطاقة VCF

`util/VCardGenerator.kt`:
1. يكتب محتوى `vCard 3.0` (مُنشأ في `domain/usecase/GenerateVCardUseCase.kt` مع ترميز
   صحيح للأحرف الخاصة وفق RFC 6350) إلى ملف مؤقت داخل `cacheDir/vcards/`.
2. يعرضه عبر `androidx.core.content.FileProvider` (مُعرَّف في `res/xml/file_paths.xml`)
   كرابط `content://` — **لا يُستخدم `file://` إطلاقًا** (محظور للمشاركة بين التطبيقات
   منذ Android 7).
3. يُشارَك عبر `Intent.ACTION_SEND` + `Intent.createChooser` (Android Sharesheet).
4. عند كل كتابة جديدة، تُحذف تلقائيًا أي بطاقات VCF أقدم من ساعة واحدة من نفس المجلد
   المؤقت (تنظيف آمن، أفضل جهد — لا يفشل الحفظ إن تعذّر الحذف).
5. إن لم يُدخل المستخدم اسمًا، يُستخدم اسم مؤقت واضح ("جهة اتصال جديدة" / حسب اللغة)
   بدل رفض العملية — القرار الأبسط والأقل احتكاكًا وفق التوجيه المطلوب.

---

## الترجمة

`res/values/strings.xml` (إنجليزي، الافتراضي)، `res/values-ar/strings.xml`،
`res/values-es/strings.xml` — ترجمة كاملة لكل نص في التطبيق، بلا أي نص مكتوب مباشرة داخل
الكود. اتجاه RTL مفعّل عبر `android:supportsRtl="true"` ويعمل تلقائيًا مع Compose. لغة
التطبيق قابلة للتبديل يدويًا من الإعدادات (بمعزل عن لغة النظام) عبر
`AppCompatDelegate.setApplicationLocales` — يعمل على كل إصدارات Android المدعومة
(minSdk 26 فما فوق)، وليس فقط Android 13+.

أرقام الهاتف الدولية (`+54 9 11 2345-6789` وغيرها) تُعرض داخل النصوص العربية عبر
`android.text.BidiFormatter.unicodeWrap(...)` في `PhonePreviewCard.kt`، لضمان عدم انعكاس
ترتيب الأرقام أو علامة `+` عند العرض داخل فقرة RTL.

---

## الصلاحيات المستخدمة

هذا التطبيق **لا يطلب أي صلاحية وقت تشغيل (runtime permission) على الإطلاق**. لا
`INTERNET`، لا `READ_CONTACTS`/`WRITE_CONTACTS`، لا `READ_CLIPBOARD` (غير موجودة أصلًا
كصلاحية Android منفصلة، لكن التطبيق لا يقرأ الحافظة إلا عند ضغط المستخدم زر "لصق" صراحة).

العنصر الوحيد المُعلَن في `AndroidManifest.xml` هو `<queries>` (وليس صلاحية) اللازم فقط
لـ:
- التحقق من تثبيت `com.whatsapp` و`com.whatsapp.w4b` (قيود ظهور الحزم في Android 11+).
- تحليل نية `ACTION_INSERT` لجهات الاتصال، ونية `ACTION_VIEW` لروابط `https` (متجر Play).

---

## الخصوصية

- لا اتصال بأي خادم خارجي إطلاقًا (لا توجد صلاحية `INTERNET` حتى لو أراد التطبيق ذلك).
- لا تحليلات ولا تتبع.
- لا تُخزَّن الأرقام افتراضيًا. ميزة "الاحتفاظ بسجل الأرقام الأخيرة" **مغلقة افتراضيًا**
  ولا تُفعَّل إلا بإجراء صريح من المستخدم في الإعدادات، مع زر مسح فوري متاح دائمًا.
- الحافظة لا تُقرأ تلقائيًا أبدًا — فقط عند ضغط زر "لصق" مباشرة.
- `res/xml/data_extraction_rules.xml` و`res/xml/backup_rules.xml` يستثنيان تفضيلات
  DataStore وملفات VCF المؤقتة من النسخ الاحتياطي السحابي لأندرويد، تحوّطًا إضافيًا.

---

## الاختبارات

### اختبارات الوحدة (`app/src/test/`) — تعمل على JVM عادي، `./gradlew test`
- `ValidatePhoneNumberUseCaseTest` — الأرجنتين (بما فيها التحويل من الصيغة المحلية `011 15-...`
  إلى `+549...`)، سوريا، روسيا، الولايات المتحدة، السعودية، الإمارات، قطر، الأردن، رقم
  غير مكتمل، رقم غير صالح، رقم بصيغة دولية مسبقة، وتعارض رمز الدولة.
- `BuildWhatsAppLinkUseCaseTest` — بناء الرابط، ترميز الرسالة (بالإنجليزية والعربية)،
  رفض رقم يحتوي `+` أو فراغات.
- `GenerateVCardUseCaseTest` — محتوى VCF، الاسم الافتراضي عند الفراغ، ترميز الأحرف
  الخاصة وفق RFC 6350.
- `PhoneNumberFormatterTest` — تنقية الإدخال أثناء الكتابة (حذف الأحرف، الإبقاء على `+`
  واحدة فقط).

> **كل رقم هاتف مستخدم في هذه الاختبارات جرى التحقق منه فعليًا** بتشغيل مباشر لمكتبة
> `libphonenumber-8.13.43.jar` (التي نزّلتها من Maven Central وشغّلتها يدويًا) قبل كتابة
> الاختبار — وليس نقلًا عن الذاكرة.

### اختبارات الواجهة (`app/src/androidTest/`) — تحتاج جهازًا/محاكيًا، `./gradlew connectedAndroidTest`
- `HomeScreenTest` — إدخال رقم صالح يُظهر "رقم صالح" ويُفعّل الأزرار، رقم غير مكتمل يُبقي
  الأزرار معطّلة، الحالة الفارغة، اختيار دولة من الشرائح السريعة، فتح "جميع الدول"
  والبحث بالرمز الدولي.
- `SettingsScreenTest` — ظهور خيارات اللغة الأربعة، خيارات المظهر الثلاثة، وأنماط
  WhatsApp الثلاثة.

---

## القيود المعروفة الأخرى (غير متعلقة بالشبكة)

- لم يُنشأ ملف APK فعلي أو لقطات شاشة نهائية ضمن هذا التسليم، لاستحالة تشغيل محاكي
  Android أو بناء المشروع فعليًا داخل هذه البيئة المعزولة (نفس قيد الشبكة أعلاه). بعد
  توليد الـ Gradle wrapper وفتح المشروع في Android Studio لديك، `./gradlew assembleDebug`
  سينتج APK جاهزًا للتجربة مباشرة.
- أيقونة التطبيق وشعار شاشة البدء مبنيّان بالكامل كـ Vector Drawables (بدون أي صور
  Raster/PNG) — تصميم بسيط يدمج سماعة هاتف مع شارة "إضافة/تواصل" صغيرة، متوافق مع
  Adaptive Icon وMaterial You، ولا يُقلّد شعار WhatsApp بأي شكل.
