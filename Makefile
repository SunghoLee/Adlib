adlib.jar: 
	@gradle build
	@cp build/libs/Adlib.jar .

clean:
	@gradle clean

