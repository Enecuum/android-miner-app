# android-miner-app

This is a so-called "white label" mining application for Enecuum Network. It can be used for creating dedicated mining apps for specific mining tokens issued in Enecuum blockchain network. 

To configure the application before building and using it, you need to write several values to the assembly files:

1. Token hash at app/build.gradle instead of 000...000 hash

```gradle
buildConfigField "String", "TOKEN", "\"0000000000000000000000000000000000000000000000000000000000000000\""
```

2. Ticker and App name at res/values/strings.xml (and other languages)

```xml
<!ENTITY ticker "COIN">
<!ENTITY name "Coin">
<!ENTITY logo "Wallet">
<!ENTITY url "https://www.coin.example">
```

Also it's necessary to replace urls for your documents and other resources.
