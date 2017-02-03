# Add a trade in the traders list

Given a trade list
When a new trade is added
Then the new trade is present into the list

## [Example 1](-)

When [adding a trade](- "#num = canAddATrade()") to an existing trade list the number of trades is increased by [1](- "?=#num")

# Add a trade in the ccp list
Given a CPP list
When a new CPP trade is added
Then the new CPP trade is present into the list

## [Example 2](-)

When [adding a CPP trade](- "#num = canAddACcpTrade()") to an existing trade list the number of trades is increased by [1](- "?=#num")