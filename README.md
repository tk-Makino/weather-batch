# Weather Batch - AWS Lambda

気象庁HPから天気図を定期的に取得し、S3に保存するAWS Lambdaバッチアプリケーションです。

## 🎯 AWS Lambda Migration Status

- ✅ **Phase 1**: S3ストレージ対応
- ✅ **Phase 2**: Spring依存削除、Lambda対応
- ✅ **Phase 3**: Lambda専用構成、デプロイ設定完了 **← 現在**
- ⬜ **Phase 4**: 本番デプロイ、運用開始

## 🏗️ アーキテクチャ

```
EventBridge (スケジュール)
    ↓
Lambda Function (weather-batch)
    ↓
気象庁HP → PDF取得 → S3バケット
```

- **実行環境**: AWS Lambda (Java 21)
- **トリガー**: EventBridge (毎時実行)
- **ストレージ**: Amazon S3
- **アーキテクチャパターン**: Hexagonal Architecture

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

# またはGradleから実行
./gradlew run --args="com.example.pdfbatch.lambda.LocalLambdaTestKt"
```

### 3. SAMでローカルテスト

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# ローカルで実行
sam local invoke WeatherBatchFunction \
  -e event.json \
  --parameter-overrides S3BucketName=your-test-bucket-name

# 環境変数を上書きして実行
sam local invoke WeatherBatchFunction \
  -e event.json \
  --parameter-overrides S3BucketName=your-test-bucket-name \
  --env-vars '{"PDF_URLS":"https://example.com/test.pdf"}'
```

### 4. Serverless Frameworkでローカルテスト

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# ローカルで実行
serverless invoke local -f fetchWeather -p event.json
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
- Parameter ScheduleExpression: `cron(0 * * * ? *)` (毎時実行)

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
    ScheduleExpression="cron(0 */6 * * ? *)"  # 6時間ごと
```

#### スタックの削除

```bash
sam delete --stack-name weather-batch
```

### 方法2: Serverless Framework

#### 初回セットアップ

```bash
# Serverless Frameworkをインストール（未インストールの場合）
npm install -g serverless

# AWS認証情報を設定
serverless config credentials \
  --provider aws \
  --key YOUR_ACCESS_KEY \
  --secret YOUR_SECRET_KEY
```

#### デプロイ

```bash
# Lambda用パッケージをビルド
./gradlew buildLambdaZip

# デプロイ（dev環境）
serverless deploy --stage dev

# デプロイ（本番環境）
serverless deploy --stage prod

# 特定の関数のみデプロイ
serverless deploy function -f fetchWeather
```

#### ログの確認

```bash
# リアルタイムログ
serverless logs -f fetchWeather -t

# 過去のログ
serverless logs -f fetchWeather --startTime 1h
```

#### スタックの削除

```bash
serverless remove --stage dev
```

## ⚙️ 設定

### 環境変数

Lambda関数で使用する環境変数：

| 環境変数 | 説明 | デフォルト値 |
|---------|------|-------------|
| `PDF_URLS` | 取得するPDFのURL（カンマ区切り） | 気象庁の天気図URL |
| `S3_BUCKET_NAME` | S3バケット名 | (必須) |
| `AWS_REGION` | AWSリージョン | `ap-northeast-1` |
| `S3_PREFIX` | S3内のプレフィックス | `pdfs/` |
| `S3_METADATA_KEY` | メタデータファイルのキー | `pdfs/metadata.json` |

### スケジュール設定

#### SAM (`template.yaml`)

```yaml
Parameters:
  ScheduleExpression:
    Type: String
    Default: cron(0 * * * ? *)  # 毎時実行
```

#### Serverless (`serverless.yml`)

```yaml
functions:
  fetchWeather:
    events:
      - schedule:
          rate: cron(0 * * * ? *)  # 毎時実行
```

#### スケジュール例

- `cron(0 * * * ? *)` - 毎時0分
- `cron(0 */6 * * ? *)` - 6時間ごと
- `cron(0 0 * * ? *)` - 毎日0時
- `cron(0 9 * * ? *)` - 毎日9時
- `rate(1 hour)` - 1時間ごと

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
│   │   ├── storage/         # S3ストレージ
│   │   └── persistence/     # メタデータ管理
│   ├── config/              # 設定管理
│   ├── di/                  # 依存性注入
│   └── lambda/              # Lambdaエントリーポイント
├── template.yaml            # SAM設定
├── serverless.yml           # Serverless Framework設定
├── event.json               # テストイベント
└── build.gradle.kts         # ビルド設定
```

## 🌳 ブランチ戦略

- `main`: AWS Lambda用（本番）
- `master`: Spring Boot版（ローカル開発用）

## 📝 ライセンス

このプロジェクトはサンプルアプリケーションです。

## 🤝 コントリビューション

Issues・Pull Requestsは歓迎です！
