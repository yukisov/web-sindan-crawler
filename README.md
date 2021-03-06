クローラ耐性診断ツール
==================

[IPA ウェブ健康診断仕様](http://www.ipa.go.jp/security/vuln/websecurity.html)の診断項目「(M)クローラへの耐性」の診断を行うコマンドラインツールです。

検索エンジンをはじめとしたWebサイトを自動で巡回するプログラムに対して、特定のWebサイトがその巡回アクセスに耐えられるかどうかを診断します。

診断対象となる1つのURLを指定すると、以下の動作を自動で繰り返して対象サイトのクローラへの耐性を診断し、最後に診断結果を表示します。

* URLにアクセスする。
* ↓  ↑
* レスポンスボディから内部リンクのURLを収集する。

※ **本プログラムは IPA とは関係ありません。非公式です。**

## IPA ウェブ健康診断仕様について
* [IPA 独立行政法人 情報処理推進機構：安全なウェブサイトの作り方](http://www.ipa.go.jp/security/vuln/websecurity.html)
	* この中の 別冊：「ウェブ健康診断仕様」(PDF)に記述されています。

##スクリーンショット
診断実行中<br/>
<a href="https://raw.github.com/wiki/yukisov/web-sindan-crawler/images/wsc_running.png" target="_blank"><img alt="実行中のスクリーンショット" src="https://raw.github.com/wiki/yukisov/web-sindan-crawler/images/wsc_running.png" width="600px"/></a>

診断完了時<br/>
<a href="https://raw.github.com/wiki/yukisov/web-sindan-crawler/images/wsc_finished.png" target="_blank"><img alt="診断完了時のスクリーンショット" src="https://raw.github.com/wiki/yukisov/web-sindan-crawler/images/wsc_finished.png" width="600px"/></a>


##開発環境
* Mac OS X 10.9.2
* Java 7
* Apache Ant v1.9.3
* Eclipse

##インストール
```
$ git clone https://github.com/yukisov/web-sindan-crawler.git
$ cd web-sindan-crawler
$ ant
```
buildディレクトリが作成されますので、このディレクトリをどこかに移動するかコピーします。(このままの位置でも構いません)

##使い方

インストールで生成されたディレクトリに移動します。

```
$ cd to/build/directory
```

診断するURLを引数にして実行します。(http(s)://は省略できます)

```
# Linux/Macの場合
$ ./WSC.sh http://www.example.com/
#Windowsの場合
> WSC.bat http://www.example.com/
```

対象サイトのクロールが開始します。画面にはアクセスログが出力され、最後に診断結果が表示されます。


##コマンドラインオプション
<pre>
usage: WSC.sh <i>URL</i>
 -l,--logfile <logfile>          log file path
 -p,--log-prop-file <log-prop-file>    log properties file
 --http-password <http-password>       password for HTTP BASIC AUTH
 --http-user <http-user>           username for HTTP BASIC AUTH
 -h,--help              show help
</pre>

##設定ファイル
confディレクトリ内に2つの設定ファイルがあります。

###config.properties
* プログラムの動作を変更できます。
* プロキシの設定
    * PROXY_HOST
        * プロキシのホスト名もしくはIPアドレス
    * AUTH_BASIC_PASSWORD
        * プロキシのポート番号
* BASIC認証の設定
    * AUTH_BASIC_USERNAME
        * BASIC認証のユーザー名
    * AUTH_BASIC_PASSWORD
        * BASIC認証のパスワード
* その他もいろいろ設定できますが、特に変更する必要はありません。

###log.properties
* ログ出力に関する設定を変更できます。
* 特に変更する必要はありません。

##出力内容
プログラム実行中、以下の項目が画面に出力されていきます。

| No.	| Elapsed | Status  | RTT  | URL |
|--------|---------|---------|-------|-----|
|通番|経過時間<br/>(分:秒)|HTTPステータスコード|応答時間(秒)|アクセスURL|

診断が終了すると以下の項目が表示されます。

| Reason for the termination | Total elapsed time | No Response Count | StatusCode 400-599 Count | Total number of requests | 診断結果 | 危険度 |
|------|------|------|------|------|------|------|
| 診断終了の理由 | 合計時間(分:秒) | タイムアウトになったリクエスト数 | ステータスコードが400番台or500番台だったレスポンスの数 | アクセスしたURLの数 | 正常 or 異常 | 中 or 低 |


##ログファイル
カレントディレクトリに2種類のログファイルが出力されます。

###WSC_年月日時分.log
* 処理中、画面に出力されたのと同じ内容が書き込まれます。
* ファイル名には、処理が開始された時刻が使用されます。

### WscInternalX.X.log
* 内部的なログが書き込まれます。
* log.properties ファイルで設定を変更できます。

##仕様
[IPA 独立行政法人 情報処理推進機構：安全なウェブサイトの作り方](http://www.ipa.go.jp/security/vuln/websecurity.html)の 別冊「ウェブ健康診断仕様」(PDF) --> [2.4. 診断時に利用する診断項目毎の検出パターン(目安)、脆弱性有無の判定基準、及び対象画面について] --> [￼(M)￼クローラへの耐性]に記載された通りの仕様になります。

* HTTP シリアルアクセスを行う。並列アクセスはしない。
* アクセス毎に 0.5秒待機する。
    * 5秒待っても反応が返ってこない場合は次のアクセスまで5秒待機する。
* aタグのhref属性からURLを取得する。
* 異なるドメインのURLは対象にしない。
* フォームに対するサブミットはしない。
* JavaScript, Javaアプレット, Flash等に含まれるURLは対象外とする。
* 最大4800ページまでアクセスする。
* 診断を終了する条件(以下はIPAの資料からほぼそのまま引用します)
    * 1. クローリングによってたどることのできる全診断対象ページへのアクセスが完了した場合
    * 2. 診断開始から 40 分が経過した場合
		    * (4800[ページ] * 0.5[秒])/60[秒] = 40[分])
    * 3. 以下の (1) ~ (6) のいずれかに該当した場合
         * (1) HTTP リクエストを送信してから、5 秒経過しても HTTP レスポンスの受信が完了しない状態が、5 回連続で発生した場合
         * (2) HTTP リクエストを送信してから、5 秒経過しても HTTP レスポンスの受信が完了しない状態が、累計で 10 回発生し、かつ以下の計算式で算出される値が 10%以上であった場合
             * (計算式) 10 ÷ アクセスの総数 × 100%
         * (3) HTTP レスポンスの HTTP ステータスコードにおいて、400 番台又は 500 番台のエラーが発生し、かつこの状態が、5 回連続で発生した場合
         * (4) HTTP レスポンスの HTTP ステータスコードにおいて、400 番台又は 500 番台のエラーが発生する状態が、累計で 10 回発生し、かつ以下の計算式で算出される値が 10%以上であった場合
             * (計算式) 10 ÷ アクセスの総数 × 100%
         * (5) HTTP リクエストを送信してから、5 秒経過しても HTTP レスポンスの受信が完了しない状態が、累計で 10 回発生し、かつ以下の計算式で算出される値が 10%未満であった場合
             * (計算式) 10 ÷ アクセスの総数 × 100%
         * (6) HTTP レスポンスの HTTP ステータスコードにおいて、400 番台又は 500 番台のエラーが発生する状態が、累計で 10 回発生し、かつ以下の計算式で算出される値が 10%未満であった場合
             * (計算式) 10 ÷ アクセスの総数 × 100%

###判定結果
* 診断の結果、終了条件(1)〜(4) のいずれかに該当する場合
    * 診断結果：異常
    * 危険度：中
* 診断の結果、終了条件(5)または(6) に該当する場合
    * 診断結果：異常
    * 危険度：低
* 診断の結果、終了条件(1)〜(6)の条件にいずれも該当しない場合
    * 診断の結果：正常


##その他の仕様
* クロールする深さを設定可能
    * 最大深さのページで見つけたURLは、それまでに見つけたURLにひと通りアクセスした後でアクセスする。
* ステータスコード3xx リダイレクトに対応（設定変更可能）
* SSLアクセス対応
* BASIC認証が必要な場合は設定ファイルに記述する。
* URL内の'#'文字以下は無視して、URLの同一性をチェックする。
* httpとhttpsだけ異なるURLを同一と見なす(設定変更可能)。
* frameタグのsrc属性からもURLを取得する。

##注意
**自分の管理していないホストに対して、このプログラムを実行しないで下さい。**

##ライセンス
* MIT License
