# Weather Batch - AWS Lambda

気象庁HPから天気図を定期的に取得し、S3に保存するAWS Lambdaバッチアプリケーション。

## 🏗️ アーキテクチャ

```
EventBridge (00 UTC スケジュール) ─┐
                                  ├─→ Lambda Function (weather-batch)
EventBridge (12 UTC スケジュール) ─┘       ↓
                                    気象庁HP → PDF取得 → S3バケット
                                                ↓
                                         YYYY/MM/DD/00/ または
                                         YYYY/MM/DD/12/
```

- **実行環境**: AWS Lambda (Java 21)
- **トリガー**: EventBridge × 2 (00 UTC と 12 UTC で別々に実行)
- **ストレージ**: Amazon S3
- **アーキテクチャパターン**: Hexagonal Architecture

## 🕐 時間帯別PDF取得

このアプリケーションは、00UTCと12UTCでそれぞれ異なるPDFを取得します。

### 動作の仕組み

1. **EventBridgeが時刻に応じて異なるパラメータを送信**
   - 00 UTC: `{"timeSlot": "00"}` を送信
   - 12 UTC: `{"timeSlot": "12"}` を送信

2. **Lambda関数が環境変数から対応するURLリストを取得**
   - `timeSlot=00` → `PDF_URLS_00` を使用
   - `timeSlot=12` → `PDF_URLS_12` を使用

3. **S3保存時にディレクトリが分かれる**
   - 00 UTC: `YYYY/MM/DD/00/filename.pdf`
   - 12 UTC: `YYYY/MM/DD/12/filename.pdf`

### ディレクトリ構造例

```
s3://weather-batch-pdfs/
  pdfs/
    2026/
      01/
        01/
          00/
            fupa252_00.pdf
            fupa302_00.pdf
            ...
          12/
            fupa252_12.pdf
            fupa302_12.pdf
            ...
        02/
          00/
            ...
          12/
            ...
```

## 📋 前提条件

### 必須
- Java 21+
- Kotlin 2.2.21
- AWS アカウント
- AWS CLI 設定済み

### デプロイツール（どちらか）
- **SAM CLI** (推奨): `brew install aws-sam-cli` / `choco install aws-sam-cli`
- **Serverless Framework**: `npm install -g serverless`

## 🚀 クイックスタート

### 1. ビルド

```bash
# Gradle でビルド
./gradlew build

# Lambda用ZIPパッケージを作成
./gradlew buildLambdaZip
```

生成されるファイル: `build/distributions/weather-batch.zip`

### 2. ローカルテスト（SAMなし）

最も簡単な方法。AWS認証情報さえあればテストできます。

```bash
# 環境変数を設定
export S3_BUCKET_NAME=your-test-bucket-name
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key

# IntelliJ IDEAで実行
# src/main/kotlin/com/example/pdfbatch/lambda/LocalLambdaTest.kt を開いて実行
# または右クリック → Run 'LocalLambdaTestKt'

# コマンドラインから実行（Gradle）
./gradlew run                      # デフォルト: timeSlot=00
./gradlew run --args="00"          # 00 UTC用のPDFを取得
./gradlew run --args="12"          # 12 UTC用のPDFを取得
```

### 3. SAMでローカルテスト

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# 00 UTC用のイベントでテスト
echo '{"timeSlot": "00"}' > event-00.json
sam local invoke WeatherBatchFunction \
  -e event-00.json \
  --parameter-overrides S3BucketName=your-test-bucket-name

# 12 UTC用のイベントでテスト
echo '{"timeSlot": "12"}' > event-12.json
sam local invoke WeatherBatchFunction \
  -e event-12.json \
  --parameter-overrides S3BucketName=your-test-bucket-name
```

### 4. Serverless Frameworkでローカルテスト

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# 00 UTC用のイベントでテスト
echo '{"timeSlot": "00"}' > event-00.json
serverless invoke local -f fetchWeather -p event-00.json

# 12 UTC用のイベントでテスト
echo '{"timeSlot": "12"}' > event-12.json
serverless invoke local -f fetchWeather -p event-12.json
```

## 🌐 AWSへのデプロイ

### 方法1: SAM CLI（推奨）

#### 初回デプロイ（ガイド付き）

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# SAM デプロイ（対話式）
sam deploy --guided
```

対話式で以下を入力：
- Stack Name: `weather-batch`
- AWS Region: `ap-northeast-1`
- Parameter S3BucketName: `your-unique-bucket-name`
- Parameter Schedule00UTC: `cron(0 0 * * ? *)` (毎日00:00 UTC)
- Parameter Schedule12UTC: `cron(0 12 * * ? *)` (毎日12:00 UTC)

設定は `samconfig.toml` に保存されます。

#### 2回目以降のデプロイ

```bash
# ビルドしてデプロイ
./gradlew buildLambdaZip
sam deploy
```

#### カスタムパラメータでデプロイ

```bash
sam deploy \
  --parameter-overrides \
    S3BucketName=my-weather-batch-bucket \
    Schedule00UTC="cron(0 0 * * ? *)" \
    Schedule12UTC="cron(0 12 * * ? *)"
```

#### スタックの削除

```bash
sam delete --stack-name weather-batch
```

## ⚙️ 設定

### 環境変数

Lambda関数で使用する環境変数：

| 環境変数 | 説明 | デフォルト値 |
|---------|------|-------------|
| `PDF_URLS_00` | 00UTCで取得するPDFのURL（カンマ区切り） | 気象庁の天気図URL (_00.pdf) |
| `PDF_URLS_12` | 12UTCで取得するPDFのURL（カンマ区切り） | 気象庁の天気図URL (_12.pdf) |
| `S3_BUCKET_NAME` | S3バケット名 | (必須) |
| `AWS_REGION` | AWSリージョン | `ap-northeast-1` |
| `S3_PREFIX` | S3内のプレフィックス | `pdfs/` |
| `S3_METADATA_KEY` | メタデータファイルのキー | `pdfs/metadata.json` |

### スケジュール設定

#### SAM (`template.yaml`)

```yaml
Parameters:
  Schedule00UTC:
    Type: String
    Default: cron(0 0 * * ? *)   # 毎日00:00 UTC
  
  Schedule12UTC:
    Type: String
    Default: cron(0 12 * * ? *)  # 毎日12:00 UTC
```

デプロイ時にカスタマイズ：

```bash
sam deploy \
  --parameter-overrides \
    S3BucketName=my-weather-batch-bucket \
    Schedule00UTC="cron(0 0 * * ? *)" \
    Schedule12UTC="cron(0 12 * * ? *)"
```

#### スケジュール例

**注意**: AWS EventBridgeのcron式は6フィールド形式で、Unix cronとは異なります。

- `cron(0 0 * * ? *)` - 毎日00:00 UTC
- `cron(0 12 * * ? *)` - 毎日12:00 UTC
- `cron(0 0,12 * * ? *)` - 毎日00:00と12:00 UTC（単一ルールで両方）
- `cron(0 */6 * * ? *)` - 6時間ごと
- `rate(12 hours)` - 12時間ごと

フォーマット: `cron(分 時 日 月 曜日 年)`
- 曜日または日のどちらかに `?` を使用する必要があります
- `*` は「すべて」を意味します

## 📊 モニタリング

### CloudWatch Logs

```bash
# SAM
sam logs --stack-name weather-batch --tail

# Serverless
serverless logs -f fetchWeather -t

# AWS CLI
aws logs tail /aws/lambda/weather-batch --follow
```

### CloudWatch メトリクス

AWS コンソール → CloudWatch → Lambda → weather-batch

確認項目：
- Invocations（実行回数）
- Duration（実行時間）
- Errors（エラー数）
- Throttles（スロットル）

## 💰 コスト見積もり

### 毎時実行（720回/月）の場合

| 項目 | 使用量 | 料金 |
|------|--------|------|
| Lambda実行 | 720回/月 | $0（無料枠内） |
| Lambda実行時間 | 72GB秒/月 | $0（無料枠内） |
| S3ストレージ | 0.72GB | $0.02/月 |
| S3リクエスト | 720回 | $0.003/月 |
| **合計** | - | **約$0.02/月（3円）** |

※ 無料枠: Lambda 100万リクエスト/月、40万GB秒/月

## 🔧 トラブルシューティング

### Lambda実行エラー

```bash
# SAM
sam logs --stack-name weather-batch

# Serverless
serverless logs -f fetchWeather
```

### S3アクセスエラー

IAMロールの権限を確認：
```bash
aws iam get-role-policy --role-name weather-batch-role --policy-name S3Access
```

### タイムアウト

`template.yaml` または `serverless.yml` でタイムアウトを延長：
```yaml
Timeout: 300  # 秒
```

### メモリ不足

メモリサイズを増やす：
```yaml
MemorySize: 1024  # MB
```

## 📁 プロジェクト構造

```
weather-batch/
├── src/main/kotlin/com/example/pdfbatch/
│   ├── domain/              # ドメインモデル
│   ├── ports/               # インターフェース定義
│   ├── application/         # ユースケース
│   ├── adapters/            # 外部システム接続
│   │   ├── http/            # HTTP通信
│   │   └── storage/         # S3ストレージ
│   ├── config/              # 設定管理
│   ├── di/                  # 依存性注入
│   └── lambda/              # Lambdaエントリーポイント
├── template.yaml            # SAM設定
└── build.gradle.kts         # ビルド設定
```

## 🌳 ブランチ戦略

- `main`: AWS Lambda用（本番）
- `master`: Spring Boot版（ローカル開発用）

## 📝 ライセンス

このプロジェクトはサンプルアプリケーションです。
