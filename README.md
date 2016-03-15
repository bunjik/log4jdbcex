[![Build Status](https://img.shields.io/travis/bunjik/log4jdbcex/master.svg)](https://travis-ci.org/bunjik/log4jdbcex)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
# log4jdbcEx

## 概要
実行したSQLをロギングするために、実際のJDBCドライバをラップするデバッグ用のJDBC 4.0ドライバです。
実際のSQLはラップしたドライバにそのまま渡され、ラップしたことにより挙動が変わることはありません。

## 使い方
このロギングドライバをを利用するためには、このドライバのjarをクラスパスに含めたうえで、接続URLを変更するだけです。

　**変更前**
  　`jdbc:h2:tcp://localhost/~/test`

　**変更後**
  　`jdbc:log4jdbcex:h2:tcp://localhost/~/test`
  　jdbc:の後ろに**log4jdbcex:**を挿入する(すべて小文字であることに注意)

これだけで、ロギングが有効化されます。
プログラム自体の修正は必要はありません。
また、java6以降では、ドライバクラス名を明示的に指定しなくても、JVMによりJDBCドライバの初期化処理が行われるようになっているため、
ドライバクラスをロギング用のものに置き換える必要も通常ありません。(ラップ前のドライバクラス名のままでよい)
もし、ドライバクラス名を明示的に指定する必要がある場合は、`info.bunji.jdbc.DriverEx`を指定してください。

## ログ出力設定について
ロード時にクラスパスをチェックし、以下の順序で利用するログライブラリを決定します。

1. [SLF4J](http://www.slf4j.org/)
2. [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/)
3. [Apache log4j](https://logging.apache.org/log4j/1.2/)
4. [Java logging API](https://docs.oracle.com/javase/8/docs/technotes/guides/logging/)

SQLのログ自体は、ロガー名：**jdbclog**のDEBUGレベルで出力されます。

## ログ出力の無効化について
ロガー名：**jdbclog**のログレベルを**NONE**にすることで、ログ出力が無効化されるだけでなく、
ログ出力のラップ処理を最低限にし、本来の実装を直接利用するようになります。
これにより、開発時等はJDBC URLを変更せずに、ログレベルの変更だけで、オーバーヘッドを最小に抑えることができます。

## webapp向けのログ表示UI

webアプリケーションのJNDI接続にこのドライバを利用した場合、GUI上から実行中のクエリが
参照するための仕組みが有効化されます。

Servlet3.0をサポートしたServetコンテナでは、web-fragment.xmlにより、自動的にUI用の
マッピングが自動追加され、利用可能となります。

**http://host[:port]/contextName/log4jdbcex/**


**TBD**
