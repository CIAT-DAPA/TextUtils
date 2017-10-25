@echo off
setLocal EnableDelayedExpansion

set limit=100000

set lineCounter=0
set filenameCounter=1
set uploaderFile=ES_3_upload.bat
set file=data.json
set name=
set extension=

del !uploaderFile!
echo curl -k -i --raw -o mapping.log -X PUT "http://localhost:9200/gbif" -H "Content-Type: application/json" -H "User-Agent: Fiddler" -H "Host: localhost:9200" --data-binary @mapping.json>>!uploaderFile! 

for %%a in (%file%) do (
	set "name=%%~na"
	set "extension=%%~xa"
)
del !name!-part*
for /f "tokens=*" %%a in (%file%) do (
	set splitFile=!name!-part!filenameCounter!!extension!
	set line=%%a
	if !lineCounter! gtr !limit! ( 
		if "!line:~2,5!" neq "index" (
			set /a filenameCounter=!filenameCounter! + 1
			set lineCounter=0
			echo curl -k -i --raw -o !splitFile!.log -X POST "http://localhost:9200/gbif/_bulk?pretty" -H "Content-Type: application/json" -H "User-Agent: Fiddler" -H "Host: localhost:9200" --data-binary @!splitFile!>>!uploaderFile!
			echo created !splitFile!
		)
	)
	echo !line!>> !splitFile!
	set /a lineCounter=!lineCounter! + 1

)
echo curl -k -i --raw -o !splitFile!.log -X POST "http://localhost:9200/gbif/_bulk?pretty" -H "Content-Type: application/json" -H "User-Agent: Fiddler" -H "Host: localhost:9200" --data-binary @!splitFile!>>!uploaderFile!
echo created !splitFile!
echo pause>>!uploaderFile!

