debug = luajava.bindClass("com.e3roid.util.Debug")

width  = 0
height = 0
x = 0
y = 0
vecx = 10
vecy = 10

forePaint = luajava.newInstance("android.graphics.Paint")
backPaint = luajava.newInstance("android.graphics.Paint")

function onLoadEngine(engine)
	width  = engine:getWidth()
	height = engine:getHeight()
	
	x = width  / 2
	y = height / 2
    
    backPaint:setARGB(255, 255, 255, 255)
    forePaint:setARGB(255, 0, 0, 0)
end

function onDraw(sprite, context) 
	canvas = sprite:getCanvas()
	    	
    canvas:drawRect(0, 0, width, height, backPaint)
	canvas:drawCircle(x, y, 20.0f, forePaint)

	if x >= width or x <= 0 then
		vecx = - vecx
	end
	if y >= height or y <= 0 then
		vecy = - vecy
	end
	
	x = x + vecx
	y = y + vecy
	
	return true
end

