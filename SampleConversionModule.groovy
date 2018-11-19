def inputMessage = binding.getVariable("inputMessage")
def outputMessage = binding.getVariable("outputMessage")
def httpRequest = binding.getVariable("httpRequest")

httpRequest.method = "POST"
httpRequest.headers = [
        "content-type": "application/json"
]
httpRequest.body = inputMessage.payload