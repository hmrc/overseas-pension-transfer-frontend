#!/bin/bash

echo ""
echo "Applying migration TransferSubmitted"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /transferSubmitted                       controllers.TransferSubmittedController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "transferSubmitted.title = transferSubmitted" >> ../conf/messages.en
echo "transferSubmitted.heading = transferSubmitted" >> ../conf/messages.en

echo "Migration TransferSubmitted completed"
