[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a3d8a40d7133497caa11051eaac6f1a2)](https://www.codacy.com/manual/kai-morich/SimpleBluetoothTerminal?utm_source=github.com&utm_medium=referral&utm_content=kai-morich/SimpleBluetoothTerminal&utm_campaign=Badge_Grade)

# 1. Aplikasi Trigger Terminal Bluetooth

Aplikasi Android ini menyediakan terminal/konsol dan tombol berorientasi baris/angka yang terhubung dengan Program Arduino untuk perangkat Bluetooth yang menerapkan Bluetooth Serial Port Profile (SPP)

Aplikasi ini mengimplementasikan koneksi RFCOMM ke jaringan yang dikenali SPP UUID 00001101-0000-1000-8000-00805F9B34FB

## 1.1. Halaman Trigger

Pada halaman awal akan muncul Device Bluetooth yang sudah tersimpan di HP.
Kemudian pilih bluetooth yang ingin dihubungkan lalu akan muncul halaman Trigger untuk menjalankan trigger.
Pada layout ini terdapat 2 tombol relay untuk menghidupkan relay 1 dan relay 2.
Terdapat timestamp untuk men-settings waktu countdown.

Pada Trigger ini terdapat 3 Mode.
Berikut adalah penjelasan untuk setiap mode dalam metode startCountdown:

### 1.1.1 Mode 1:

- Timer hitung mundur tunggal dimulai untuk durasi yang ditentukan.
- Selama setiap detik, sisa waktu dalam detik ditampilkan.
- Ketika timer selesai, kedua relay (Relay 1 dan Relay 2) dihidupkan.

### 1.1.2 Mode 2:

- Mode ini melibatkan dua tahap hitung mundur.
- Pada tahap pertama, durasi diatur untuk Relay 1, dan pengguna diminta untuk mengatur waktu untuk Relay 2.
- Pada tahap kedua, timer hitung mundur dimulai untuk Relay 1. Ketika selesai, Relay 1 dihidupkan, dan timer hitung mundur lain dimulai untuk Relay 2. Ketika timer kedua ini selesai, kedua relay dihidupkan.

### 1.1.3 Mode 3:

- Mode ini juga melibatkan dua tahap hitung mundur tetapi dengan logika yang berbeda.
- Pada tahap pertama, timer hitung mundur dimulai untuk Relay 1. Ketika selesai, Relay 1 dihidupkan.
- Pada tahap kedua, timer hitung mundur lain dimulai untuk Relay 2. Ketika selesai, Relay 2 dihidupkan.
