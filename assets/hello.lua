debug = luajava.bindClass("com.e3roid.util.Debug")

date = luajava.newInstance("java.util.Date")
debug:d(date:toString())

count = 0

function hello(msg1, msg2)
    str = msg1 .. "," .. msg2 .. " from Lua!"
    debug:d(str)
    return str
end

function countup() 
	count = count + 1
    return count
end

function rotate(sprite)
	angle = sprite:getAngle() + 20
	sprite:rotate(angle)
end