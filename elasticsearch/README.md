# elasticsearch
elasticsearchを試す場所

# 起動方法
```
$ docker-compose up -d
```
elasticsearchとelastic-hqが起動する。  
elasticsearchは9200, 9300ポート  
elastic-hqはelasticsearchの監視用アプリケーションで、localhost:5000をブラウザで開く  
elastic-hqを開くと最初にelasticsearchの場所を聞かれるので `http://elasticsearch:9200` に接続すれば良い