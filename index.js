var app         = require('express')();
var http        = require('http').Server(app);
var io          = require('socket.io')(http);
var crypto      = require('crypto');
var QRCode      = require('qr-image');
var url         = require('url');
var template    = require("swig");

//const indexj2 = template.compileFile(__dirname + "/index.html.j2");
const voidj2 = template.compileFile(__dirname + "/voidhandler.html.j2");
const SERVER_PORT=3000;
const PSK_LENGTH=10;

// 1: The server is listening.
http.listen(SERVER_PORT, function(){
        console.log('listening on *:' + SERVER_PORT + ".");
        });


// 2: Browser client comes to sign-in
app.get('/', function(req, res){
       // res.writeHead(200,{ 'Content-Type': 'text/html'  }); 
        var randomid = crypto.randomBytes(PSK_LENGTH).toString("hex");
        console.log("Generating PSK: " + randomid + "." );
        let render = template.renderFile(__dirname + "/index.html.j2", {
                id: randomid
                });
        res.send(render);
        });
// 2: Browser queries QR-Code with right PSK:
app.get('/authentication.svg', function(req, res){
        var queryString = url.parse(req.url, true).query;
        if(queryString.id) {
            var qr = QRCode.image('http://localhost:3000/authhandler?id=' + queryString.id, { type: 'svg' });
            res.type('svg');
            qr.pipe(res);
            };
        });

io.on('connection', function(socket){
        console.log('Client connected.');
        var connRandomId;
        // Browser wants to join a room
        socket.on('id', function(randomid){
                connRandomId = randomid;
                console.log('using random ID: ' + randomid + ".");
                socket.join(randomid);
                });
        // New device (e.g. smartphone) Wants to check the ID
        socket.on('authid', function(id){
                connRandomId = id;
                console.log('using session ID: ' + id + ".");
                socket.join(randomid);
                socket.emit("uuidquery");
                });
        // New device sends UUID.
        socket.on('uuidresponse', function(uuid){
                console.log('Got UUID: ' + uuid + ".");
                // PUT HERE THE BACKEND STORING UUID.
                socket.in(connRandomId).emit("Successful Bind", uuid);
                socket.emit("uuidquery");
                });
        socket.on('disconnect', function(){
                console.log('client disconnected');
                });
        // For each update, tell other device.
        socket.on('broadcast', function(payload){
                socket.to(connRandomId).emit(payload)
                });
        });

// 4: Mobile app has snapped the QR. Let's open a void webpage with ID Socket binding.
app.get('/authhandler', function(req, res){
        //res.sendFile(__dirname + '/voidhandler.html');
        var queryString = url.parse(req.url, true).query;
        if(queryString.id) {
        //res.writeHead(200,{ 'Content-Type': 'text/html'  }); 
            io.to(queryString.id).emit("", );
            };
        });

