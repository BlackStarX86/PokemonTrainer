debug = com.e3roid.util.Debug;

date = new java.util.Date();
debug.d(date.toString());

count = 0;

function hello(msg1, msg2) {
    str = msg1 + "," + msg2 + " from JavaScript!";
    debug.d(str);
    return str;
}

function countup() {
	count = count + 1;
    return count;
}

function rotate(sprite) {
	angle = sprite.getAngle() + 20;
	sprite.rotate(angle);
}