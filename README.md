# 📚 SmartExam

SmartExam, Android cihazlarda sanal sınav yapmayı ve sınav sonuçlarının yönetilmesini sağlayan bir **mobil uygulama** projesidir. Bu proje, öğrencilerin sınavlara katılmasını, sonuçlarını görmesini ve eğitmenlerin sınav içeriğini yönetmesini hedefler.

---

## 🧠 Özellikler

* 📱 Android platformu için geliştirilmiş mobil uygulama
* 👩‍🎓 Kullanıcı (öğrenci) sınava giriş
* 📝 Sınav sorularını görüntüleme ve cevaplama
* 📊 Sonuçların değerlendirilmesi
* 🔐 Basit kimlik doğrulama (varsa)
* 📦 Modüler ve ölçeklenebilir kod yapısı

---

## 🛠️ Kullanılan Teknolojiler

| Bileşen            | Teknoloji / Dil                 |
| ------------------ | ------------------------------- |
| Programlama Dili   | Kotlin                          |
| Platform           | Android                         |
| Android Gradle DSL | Kotlin-based `build.gradle.kts` |
| IDE (önerilen)     | Android Studio                  |

📌 **Kullanılan ana teknoloji:** *Kotlin* ile Android uygulaması geliştirme. ([GitHub][1])

---

## 📱 Test/Sunum Platformları

Uygulama aşağıdaki platformlarda test edilip çalıştırılabilir:

* 🤖 Android Emulator (Android Studio)
* 📱 Fiziksel Android cihazlar

> Eğer projenin test raporları veya cihaz uyumluluk detayları eklenecekse bu bölüm genişletilebilir.

---

## 📁 Proje Yapısı

```
SmartExam/
├── app/                        # Android uygulama modülü
├── gradle/                     # Gradle wrapper
├── .gitignore
├── LICENSE
├── build.gradle.kts            # Proje yapılandırması (Kotlin DSL)
├── settings.gradle.kts
└── SmartExam Sanal Sınav uygulaması.pdf # Teknik doküman (PDF)
```

---

## 🚀 Kurulum & Çalıştırma

Aşağıdaki adımlarla proje Android Studio’da açılabilir ve çalıştırılabilir:

1. Depoyu klonlayın:

   ```bash
   git clone https://github.com/utkuakcicek134/SmartExam.git
   ```

2. Android Studio ile açın:

   * Android Studio’yu açın
   * `SmartExam/` klasörünü *Open Existing Project* ile seçin

3. Gradle yapılandırmasını senkronize edin (Sync Now)

4. Bir **Android Emulator** oluşturun veya fiziksel cihaz bağlayın

5. “Run” butonuna tıklayarak uygulamayı başlatın

---

## 📌 Gereksinimler

* Android Studio Arctic Fox veya daha yeni sürüm
* Android SDK (API 21+ önerilir)
* Gradle 7.x
* Kotlin 1.5+ (projede DSL ile belirtilmiş olabilir)

---

## 🧪 Geliştirme İpuçları

🔹 Geliştirme yaparken `SmartExam Sanal Sınav uygulaması.pdf` dosyasını referans olarak kullanabilirsiniz (varsa tasarım & akış bilgileri). ([GitHub][1])

🔹 Uygulama ileride Firebase, REST API veya çevrim içi sınav sistemlerine entegre edilebilir.

---

## 📝 Lisans

Bu proje **MIT License** ile lisanslanmıştır. ([GitHub][1])

---

## 📬 İletişim

Herhangi bir sorunuz olduğunda proje issues kısmını kullanabilir veya GitHub profil üzerinden mesajlaşabilirsiniz.

---

📌 *İstersen projeye göre bu README’i daha da genişletebilir (örn. ekran görüntüleri, API detayları, UI akış şemaları, Katkı kılavuzu vs.). Talep edersen bunu da yapabilirim!*

---
