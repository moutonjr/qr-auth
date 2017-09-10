var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var crypto = require('crypto');
var QRCode = require('qr-image');

const SERVER_PORT=3000;
const SALT_LENGTH=10;

http.listen(SERVER_PORT, function(){
         console.log('listening on *:' + SERVER_PORT + ".");
         });


app.get('/', function(req, res){
            qr.pipe(res);
            //res.sendFile(__dirname + '/index.html');
        });

io.on('connection', function(socket){
        var randomid = crypto.randomBytes(SALT_LENGTH).toString("hex")
        console.log("Generating UUID: " + randomid + "." );
        qr=QRCode.image('http://localhost:3000/handler?id=' + randomid, { type: 'svg' });
        res.type('svg');
        io.emit('chat message', "User user-" + user + " is connected");
        socket.on('disconnect', function(){
                console.log('client disconnected');
                });
        
});
