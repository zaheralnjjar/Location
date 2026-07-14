# libphonenumber ships its metadata as resources and reflects on some geocoding classes we don't use.
-dontwarn com.google.i18n.phonenumbers.**
-keep class com.google.i18n.phonenumbers.** { *; }
