## HiBot WebView Android

---

Repositório de testes para implementações do HiBot em aplicativos Android. 
A implementação foi feita em Kotlin e em breve será disponibilizada uma versão Java.


#### Pontos de atenção:

------------------
* O HiBot funciona com suporte total a todas as suas funcionalidades apenas em dispositivos cuja versão do Android está acima da 5.0;
* Android 4.2+: Suporte parcial, dispositivos com versão inferior a essa não são compatíveis com as APIs utilizadas para a funcionalidade de envio de áudio;


#### Executando o projeto

---

##### Downloads necessários:
* [Android Studio 3.0+](https://developer.android.com/studio/)

##### Configurações
No arquivo `strings.xml`, encontram-se declarados os seguintes parâmetros:
* **base_url**: Url utilizada para o carregamento do Bot. Para testes locais, é necessário utilizar `https://SEU_IP_LOCAL:3001/`. Para testes em QA, utilizar: `https://bot.qa.directtalk.com.br/1.0/staticbot`
* **token**: ID do departamento de onde o bot deverá ser carregado;
* **bot_params**: Demais parâmetros a serem passados para o HiBot;

No arquivo `MainActivity.kt`, o parâmetro `bypassLocalSSL` é utilizado para ignorar erros no certificado local e de QA.


##### Permissões necessárias
Para que o HiBot funcione apropriadamente, são necessárias as seguintes permissões declaradas no `Manifest.xml`:
* INTERNET;
* ACCESS_NETWORK_STATE;
* WRITE_EXTERNAL_STORAGE (Apenas para utilização do bloco upload);
* READ_EXTERNAL_STORAGE (Apenas para utilização do bloco upload);
* CAMERA (Apenas para utilização do bloco upload);
* RECORD_AUDIO (Apenas para a funcionalidade de envio de áudio);
* MODIFY_AUDIO_SETTINGS (Apenas para a funcionalidade de envio de áudio);

Para o funcionamento dentro do WebView, é necessário habilitar as seguintes configurações:

_Kotlin_

```
        webViewSettings.javaScriptEnabled = true
        webViewSettings.domStorageEnabled = true
        webViewSettings.allowFileAccess = true
```

_Java_

```
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setAllowFileAccess(true);
```