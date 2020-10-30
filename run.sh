./start-server.sh &

sleep 2

cd listener

gradle run &

cd ..

sleep 2

./start-client.sh