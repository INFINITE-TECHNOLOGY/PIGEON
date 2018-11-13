def inputMessage = binding.getVariable("inputMessage")
def outputMessage = binding.getVariable("outputMessage")
def httpMessage = binding.getVariable("httpMessage")

httpMessage.method = "POST"
httpMessage.headers = [
        "content-type": "application/json"
]
httpMessage.body = inputMessage.payload