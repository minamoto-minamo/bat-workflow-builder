# 拡張タスクスケジューラー要件定義書 (Ver.1.0)

## 第1章 システム概要

拡張タスクスケジューラーは、Webブラウザ上でワークフローを視覚的に定義・管理し、各ステップを時間・依存関係・並列性・リトライ設定に基づいて.batファイル単位で実行する系統である。

## 第2章 技術選定

| 分類          | 技術選定                       | 理由                           |
| ----------- | -------------------------- | ---------------------------- |
| フロントエンド     | React                      | SPA構成に最適                     |
| フロー構築UI     | react-flow                 | React特化のノードベースUIを実現          |
| 統計チャート      | Recharts                   | Reactとの親和性が高く、軽量で使いやすい       |
| バックエンド      | Java 21 + Spring Boot      | 最新LTSであり、DIやREST API構築に最適    |
| スケジューラーエンジン | Quartz Scheduler           | cron表現、依存管理、リトライ、並列制御など多機能対応 |
| バッチ実行方法     | ProcessBuilder             | .batファイルの実行にネイティブ対応          |
| データベース      | SQL Server                 | 商用に耐えるスケーラブルなDBMS            |
| ログ管理        | SLF4J + Logback + Actuator | ログ管理 + モニタリングAPI連携           |

## 第3章 DB設計

### ステップ定義 (step)

| カラム名        | データ型           | 説明                      |
| ----------- | -------------- | ----------------------- |
| id          | INT            | ステップID。主キー（自動採番）        |
| name        | NVARCHAR(100)  | ステップ名                   |
| bat\_path   | NVARCHAR(512)  | 実行対象の .bat ファイルの絶対パス    |
| memo        | NVARCHAR(1000) | ステップに関する補足メモ情報          |
| is\_deleted | BIT            | 論理削除フラグ（1: 削除済 / 0: 有効） |

```sql
CREATE SEQUENCE seq_step START WITH 200001 INCREMENT BY 1;

CREATE TABLE step (
    id INT PRIMARY KEY DEFAULT NEXT VALUE FOR seq_step,
    name NVARCHAR(100) NOT NULL,
    bat_path NVARCHAR(512) NOT NULL,
    memo NVARCHAR(1000),
    is_deleted BIT DEFAULT 0
);
```

### ワークフロー定義 (workflow)

| カラム名        | データ型           | 説明                      |
| ----------- | -------------- | ----------------------- |
| id          | INT            | ワークフローID（自動採番）          |
| name        | NVARCHAR(100)  | ワークフロー名                 |
| flow\_json  | NVARCHAR(MAX)  | フロー構成を定義するJSON文字列       |
| memo        | NVARCHAR(1000) | ワークフローに関する補足メモ情報        |
| is\_deleted | BIT            | 論理削除フラグ（1: 削除済 / 0: 有効） |
| created\_at | DATETIME2      | レコード作成日時（デフォルト: 現在時刻）   |
| updated\_at | DATETIME2      | レコード更新日時（デフォルト: 現在時刻）   |

```sql
CREATE SEQUENCE seq_workflow_id START WITH 100001 INCREMENT BY 1;

CREATE TABLE workflow (
    id INT PRIMARY KEY DEFAULT NEXT VALUE FOR seq_workflow_id,
    name NVARCHAR(100) NOT NULL,
    flow_json NVARCHAR(MAX) NOT NULL,
    memo NVARCHAR(1000),
    is_deleted BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
```

### 実行履歴 (step\_execution\_log)

| カラム名           | データ型          | 説明                                                   |
| -------------- | ------------- | ---------------------------------------------------- |
| id             | INT           | 実行ログID（自動採番）                                         |
| workflow\_id   | INT           | 対象ワークフローID（workflow テーブルの外部キー）                       |
| step\_id       | INT           | 対象ステップID（step テーブルの外部キー）                             |
| status         | NVARCHAR(20)  | 実行状態（WAITING / RUNNING / SUCCESS / FAILED / SKIPPED） |
| started\_at    | DATETIME2     | ステップ実行開始日時                                           |
| ended\_at      | DATETIME2     | ステップ実行終了日時                                           |
| exit\_code     | INT           | バッチプロセスの終了コード                                        |
| log\_path      | NVARCHAR(512) | 実行ログファイルの保存先パス                                       |
| error\_message | NVARCHAR(MAX) | エラー発生時のメッセージ内容                                       |

```sql
CREATE SEQUENCE seq_execution_log START WITH 500001 INCREMENT BY 1;

CREATE TABLE step_execution_log (
    id INT PRIMARY KEY DEFAULT NEXT VALUE FOR seq_execution_log,
    workflow_id INT NOT NULL,
    step_id INT NOT NULL,
    status NVARCHAR(20),
    started_at DATETIME2,
    ended_at DATETIME2,
    exit_code INT,
    log_path NVARCHAR(512),
    error_message NVARCHAR(MAX),
    FOREIGN KEY (workflow_id) REFERENCES workflow(id),
    FOREIGN KEY (step_id) REFERENCES step(id)
);
```

※ すべての ID はシーケンスによって自動採番され、以下の規則を持つ：

* workflow: 100001 から開始
* step: 200001 から開始
* step\_execution\_log: 500001 から開始

## 第4章 flow\_json スキーマ

```json
{
  "nodes": [
    {
      "id": "step-001",
      "position": { "x": 100, "y": 200 },
      "data": {
        "stepId": "200001",
        "retryCount": 2,
        "retryIntervalSec": 30,
        "parallel": true,
        "schedule": {
          "type": "weekly",
          "time": "14:00",
          "daysOfWeek": ["MON", "TUE"]
        }
      }
    }
  ],
  "edges": [
    {
      "id": "e1-2",
      "source": "step-001",
      "target": "step-002",
      "type": "default"
    }
  ]
}
```

### ノード情報の構造

| 項目名                             | 説明                      |
| ------------------------------- | ----------------------- |
| `nodes[].id`                    | フロントエンドで使用する一意なノードID    |
| `nodes[].position`              | ノードの画面上表示位置（x, y座標）     |
| `nodes[].data.stepId`           | ステップテーブル（step）のIDと対応    |
| `nodes[].data.retryCount`       | 処理失敗時にリトライを行う最大回数       |
| `nodes[].data.retryIntervalSec` | リトライ間隔（秒単位）             |
| `nodes[].data.parallel`         | true の場合、他ステップとの並列実行を許容 |
| `nodes[].data.schedule`         | スケジュール条件を定義するオブジェクト     |

### スケジュール定義の詳細

| キー名              | 使用条件                   | 値の形式                                | 説明             |
| ---------------- | ---------------------- | ----------------------------------- | -------------- |
| `type`           | 必須                     | `daily` `weekly` `monthly` `yearly` | 実行スケジュールの種類    |
| `time`           | 必須                     | "HH\:mm"                            | 実行する時間（24時間表記） |
| `daysOfWeek`     | `type = weekly` のとき有効  | \["MON", "TUE"]                     | 実行する曜日（月曜〜日曜）  |
| `daysOfMonth`    | `type = monthly` のとき有効 | \[1, 15, 31]                        | 実行する月日（1～31）   |
| `lastDayOfMonth` | `type = monthly` のとき任意 | true / false                        | 月末を実行対象とするか    |
| `daysOfYear`     | `type = yearly` のとき有効  | \["01-15", "06-30"]                 | MM-DD形式で指定     |

### edges 情報

| 項目名              | 説明                                         |
| ---------------- | ------------------------------------------ |
| `edges[].id`     | エッジ（依存関係）の一意なID                            |
| `edges[].source` | 依存元ステップID。完了後に `target` が実行可能となる           |
| `edges[].target` | 実行対象ステップID。指定された `source` ステップの完了後に起動対象となる |
| `edges[].type`   | エッジの種別。通常は `default` を使用                   |

※ `edges[]` が空の場合、そのステップは依存が存在せず、スケジュール条件のみで実行されます。

## 第5章 実行エンジンロジック

### 実行制御仕様

| 項目         | 内容                                                |
| ---------- | ------------------------------------------------- |
| 依存関係処理     | `edges` による依存ステップがすべて完了後に実行開始                     |
| 並列実行       | `parallel = true` が指定されたステップは他と並行で実行              |
| リトライ処理     | `retryCount`, `retryIntervalSec` の条件に従って再試行       |
| 実行制御       | Quartz Scheduler によりステップごとに Job/Trigger を生成       |
| 状態管理       | 実行状態を DB に記録し、次のステップの実行判断に利用                      |
| Process 管理 | Java の `Map<String, Process>` により実行中のプロセスを制御・監視可能 |

## 第6章 手動操作

| 操作   | 内容                                                       |
| ---- | -------------------------------------------------------- |
| 手動開始 | スケジュールや依存関係を無視して即時実行                                     |
| 強制終了 | 実行中の `Process` に対して `destroy()` / `destroyForcibly()` 実行 |
| スキップ | 依存元ステップが完了しなくても、ステップを実行済みとみなして次に進める                      |
| 再実行  | `SUCCESS` や `FAILED` のステップを再実行                           |

## 第7章 設定管理

| 設定項目                  | 内容例                      | 用途                |
| --------------------- | ------------------------ | ----------------- |
| `spring.datasource.*` | DB接続設定（URL, ユーザー名、パスワード） | DBアクセス用           |
| `logging.file.path`   | ログ出力先ディレクトリ              | 実行ログ保存            |
| `scheduler.retry.*`   | リトライのデフォルト設定など           | スケジューラー初期値定義      |
| `application.yml`     | 全体設定を一元管理                | Spring Boot設定ファイル |
