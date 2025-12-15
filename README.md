# PDF Batch Application

Kotlin + Spring Boot 4.0.0 で構築された、OkHttp を使用したPDF定期取得バッチアプリケーションです。
Hexagonal Architecture（ポート&アダプター）パターンを採用しています。

## 特徴

- **Hexagonal Architecture**: ドメインロジックとインフラストラクチャの分離
- **OkHttp**: 効率的なHTTPクライアント
- **差分検知**: SHA-256ハッシュによる変更検知
- **スケジュール実行**: Spring Schedulingによる定期実行
- **メタデータ管理**: JSON形式での履歴管理

## アーキテクチャ

```
src/main/kotlin/com/example/pdfbatch/
├── Application.kt                          # エントリーポイント
├── domain/
│   └── Metadata.kt                         # ドメインモデル
├── ports/
│   └── Ports.kt                            # インターフェース定義
├── application/
│   └── PdfFetchService.kt                  # アプリケーションサービス（ユースケース）
├── adapters/
│   ├── http/
│   │   └── OkHttpPdfDownloader.kt         # HTTP通信アダプター
│   ├── storage/
│   │   └── FileSystemStorage.kt           # ファイルストレージアダプター
│   └── persistence/
│       └── JsonMetadataRepository.kt      # JSONリポジトリアダプター
├── config/
│   └── OkHttpConfig.kt                     # OkHttp設定
└── entrypoints/
    └── RunnerConfigAndScheduled.kt        # 起動・スケジュール制御
```

## 設定

`src/main/resources/application.yml` で設定を変更できます：

```yaml
pdf:
  # 取得対象のURL（カンマ区切りで複数指定可能）
  urls: https://example.com/sample.pdf,https://example.com/another.pdf
  
  # 保存先ディレクトリ
  storage:
    directory: ./data/pdfs
  
  # 実行設定
  fetch:
    run-on-startup: true          # 起動時に実行
    cron: "0 0 * * * *"           # 毎時0分に実行
```

### Cron式の例

- `0 0 * * * *` - 毎時0分
- `0 */30 * * * *` - 30分ごと
- `0 0 9 * * *` - 毎日9時
- `0 0 */6 * * *` - 6時間ごと

## ビルドと実行

### 前提条件

- Java 21+
- Kotlin 2.2.21

### ビルド

```bash
# Windows (PowerShell)
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

### 実行

```bash
# Windows (PowerShell)
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

または、ビルド後のJARを実行：

```bash
java -jar build/libs/weather-batch-0.0.1-SNAPSHOT.jar
```

### コマンドライン引数での設定上書き

```bash
java -jar build/libs/weather-batch-0.0.1-SNAPSHOT.jar \
  --pdf.urls=https://example.com/test.pdf \
  --pdf.storage.directory=./custom/path \
  --pdf.fetch.cron="0 */10 * * * *"
```

## 動作の流れ

1. **起動時実行** (オプション)
   - アプリケーション起動時に即座にPDF取得を実行

2. **スケジュール実行**
   - 設定されたcron式に従って定期的にPDF取得を実行

3. **PDF取得プロセス**
   - 設定されたURLからPDFをダウンロード
   - SHA-256ハッシュを計算
   - 既存のメタデータと比較
   - 差分がある場合のみ保存
   - メタデータを更新

## 出力

### PDFファイル

`./data/pdfs/` ディレクトリに以下の形式で保存されます：

```
pdf_20251215_143000_a1b2c3.pdf
```

- タイムスタンプ: yyyyMMdd_HHmmss
- URLハッシュ: 6文字の16進数

### メタデータ

`./data/pdfs/metadata.json` に履歴が保存されます：

```json
{
  "items": [
    {
      "url": "https://example.com/sample.pdf",
      "filename": "pdf_20251215_143000_a1b2c3.pdf",
      "hash": "abc123...",
      "downloadedAt": "2025-12-15T14:30:00",
      "size": 123456
    }
  ]
}
```

## Hexagonal Architecture について

このアプリケーションは以下のレイヤーで構成されています：

- **Domain**: ビジネスロジックとドメインモデル
- **Ports**: インターフェース定義（入力/出力）
- **Application**: ユースケース実装
- **Adapters**: 外部システムとの接続実装
- **Entrypoints**: アプリケーションの起動ポイント

この構造により、テスタビリティと保守性が向上し、外部依存の交換が容易になります。

## トラブルシューティング

### URLが取得できない場合

- ネットワーク接続を確認
- URLが正しいか確認
- ファイアウォール設定を確認

### ファイルが保存されない場合

- `pdf.storage.directory` のパスに書き込み権限があるか確認
- ディスク容量を確認

### スケジュールが動作しない場合

- cron式が正しいか確認
- ログレベルをDEBUGに設定して詳細を確認

## ライセンス

このプロジェクトはサンプルアプリケーションです。

