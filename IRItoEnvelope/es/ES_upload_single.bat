curl -k -i --raw -o mapping.log -X PUT "http://localhost:9200/iri" -H "Content-Type: application/json" -H "Host: localhost:9200" --data-binary @mapping.json 
curl -k -i --raw -o data.log -X POST "http://localhost:9200/iri/_bulk?pretty" -H "Content-Type: application/json" -H "Host: localhost:9200" --data-binary @data.json
call ES_4_status.bat
