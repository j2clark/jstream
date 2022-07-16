# To Proxy or NOT to Proxy

## Non Proxy Approach

Lambda is tasked with handling a well designed data model
Web specific issues usch as CORS are outside the scope of the lambda
This makes the lambda a true function, dealing only with the logical task at hand - code (lambda) is reusable by various services, not just ApiGateway

The drawback is added integration complexity - The API Gateway configuration is tasked with mapping request and response attrivutes correctly, dealing with CORS headsers, etc.  

## Proxy Approch

AWS_PROXY forces the lambda function handle web specific duties, such as returning appropriate CORS headers.