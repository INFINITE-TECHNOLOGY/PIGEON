def inputMessage = binding.getVariable("inputMessage")
def outputMessage = binding.getVariable("outputMessage")
def httpRequest = binding.getVariable("httpRequest")

httpRequest.method = "POST"
httpRequest.headers = [
        "content-type": "application/json",
        "Host"        : "somehost"
]
httpRequest.body = inputMessage.payload