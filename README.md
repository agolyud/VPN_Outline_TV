# VPN Out BOX — client for Android, Android TV, and Google TV

<img src="https://github.com/user-attachments/assets/9e2fc215-89a4-4f47-ae37-eef83532115b" width=30% height=30%>
<img src="https://github.com/user-attachments/assets/8f504d1c-0aca-4d1a-8df1-c5c85eae2f54" width=30% height=30%>
<img src="https://github.com/user-attachments/assets/f5c59cdc-86db-4d2d-ab64-a2614f551da3" width=30% height=30%>
<img src="https://github.com/user-attachments/assets/0f7f1fc6-f8a9-4d88-9423-125726cc3b45" width=30% height=30%>
<img src="https://github.com/user-attachments/assets/8575cc52-7d1d-43f3-92d1-2206c24462dc" width=30% height=30%>



**VPN Out BOX** is an open-source VPN application for devices running **Android**, **Android TV**, and **Google TV**.  
It was originally created as a lightweight version of Outline VPN with a simplified interface, but over time it has significantly expanded its capabilities and now supports a wide range of modern protocols while still remaining easy to use.

The app makes it easy to connect to your own VPN server, providing reliable traffic encryption and privacy protection.

## ✨ Features

- **Multi-platform support** — works on **Android**, **Android TV**, and **Google TV**
- **Secure connection** — protects your traffic using modern VPN and proxy protocols
- **User-friendly interface** — simple and intuitive UI optimized for both smartphones and TVs
- **Flexible configuration** — manage key VPN settings directly inside the app
- **Privacy-focused** — the app does not keep user activity logs
- **Fast and stable connection** — optimized for reliable performance across different device types
- **Automatic best protocol selection by ping** — the app can automatically determine the fastest connection method
- **Automatic best key selection by ping** — quickly selects the most responsive key
- **Manual optimal key selection** — lets you choose the best key yourself based on ping
- **One-click key switching** — quickly switch between available keys
- **QR code key transfer** — on **TV**, a QR code is displayed for scanning from a phone; on smartphones, keys can be added directly through the camera
- **App whitelist** — VPN can be applied only to selected apps
- **Auto-connect** — the app can automatically connect to VPN when launched

## Supported Protocols

| Protocol | Scheme | Support / Features |
|---|---|---|
| Shadowsocks / Outline | `ss://` | SIP002, SIP008, AEAD (AES-128/256-GCM, ChaCha20-Poly1305) |
| Hiddify | Hiddify keys | Hiddify key support |
| VMess | `vmess://` | WS, gRPC, HTTP/2, Auto Secure, Packet Encoding |
| VLESS | `vless://` | Reality, Vision, XTLS Flow, uTLS |
| Trojan | `trojan://` | Trojan-Go compatible, Mux |
| Hysteria 2 | `hysteria2://` | QUIC, Port Hopping, Congestion Control |
| TUIC v5 | `tuic://` | 0-RTT, BBR, QUIC transport |
| WireGuard | `wireguard://` | VPN tunnel, PSK |
| SSH | `ssh://` | Secure tunnel, Private Key authentication |
| AnyTLS | `anytls://` | TLS wrapper, traffic obfuscation |
| Naive | `naive+https://` | Native sing-box support |
| HTTPS | HTTPS keys | HTTPS key support |

__________________________________________________________________________________________________________________________
Russian Version

# VPN Out BOX — клиент для Android, Android TV и Google TV

**VPN Out BOX** — это open-source VPN-приложение для устройств на базе **Android**, **Android TV** и **Google TV**.  
Изначально оно создавалось как облегчённая версия Outline VPN с упрощённым интерфейсом, но со временем значительно расширило свои возможности и теперь поддерживает широкий набор современных протоколов, сохраняя при этом простоту использования.

Приложение позволяет легко подключаться к собственному VPN-серверу, обеспечивая надёжное шифрование трафика и защиту конфиденциальности.

## ✨ Особенности

- **Поддержка нескольких платформ** — приложение работает на **Android**, **Android TV** и **Google TV**
- **Безопасное подключение** — защита трафика с использованием современных VPN- и proxy-протоколов
- **Удобный интерфейс** — простой и понятный UI, адаптированный как для смартфонов, так и для телевизоров
- **Гибкая настройка** — управление основными параметрами VPN прямо внутри приложения
- **Конфиденциальность** — приложение не ведёт журнал активности пользователя
- **Быстрое и стабильное соединение** — оптимизировано для надёжной работы на разных типах устройств
- **Автоматический выбор лучшего протокола по пингу** — приложение может самостоятельно определить наиболее быстрый способ подключения
- **Автоматический выбор лучшего ключа по пингу** — быстрый выбор наиболее отзывчивого ключа
- **Ручной выбор оптимального ключа** — возможность самостоятельно выбрать лучший ключ на основе пинга
- **Переключение между ключами в один клик** — быстрая смена доступных ключей
- **Передача ключей через QR-код** — на **TV** отображается QR-код для сканирования с телефона, а на смартфонах ключ можно добавить через камеру
- **Белый список приложений** — VPN можно применять только к выбранным приложениям
- **Автоподключение** — приложение может автоматически подключаться к VPN при запуске

## Поддерживаемые протоколы

| Протокол | Схема | Поддержка / особенности |
|---|---|---|
| Shadowsocks / Outline | `ss://` | SIP002, SIP008, AEAD (AES-128/256-GCM, ChaCha20-Poly1305) |
| Hiddify | ключи Hiddify | Поддержка ключей Hiddify |
| VMess | `vmess://` | WS, gRPC, HTTP/2, Auto Secure, Packet Encoding |
| VLESS | `vless://` | Reality, Vision, XTLS Flow, uTLS |
| Trojan | `trojan://` | Совместимость с Trojan-Go, Mux |
| Hysteria 2 | `hysteria2://` | QUIC, Port Hopping, Congestion Control |
| TUIC v5 | `tuic://` | 0-RTT, BBR, QUIC transport |
| WireGuard | `wireguard://` | VPN tunnel, PSK |
| SSH | `ssh://` | Secure tunnel, Private Key authentication |
| AnyTLS | `anytls://` | TLS wrapper, traffic obfuscation |
| Naive | `naive+https://` | Native sing-box support |
| HTTPS | HTTPS-ключи | Поддержка HTTPS-ключей |
