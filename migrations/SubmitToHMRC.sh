#!/bin/bash

echo ""
echo "Applying migration SubmitToHMRC"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /submitToHMRC                        controllers.SubmitToHMRCController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /submitToHMRC                        controllers.SubmitToHMRCController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSubmitToHMRC                  controllers.SubmitToHMRCController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSubmitToHMRC                  controllers.SubmitToHMRCController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "submitToHMRC.title = submitToHMRC" >> ../conf/messages.en
echo "submitToHMRC.heading = submitToHMRC" >> ../conf/messages.en
echo "submitToHMRC.checkYourAnswersLabel = submitToHMRC" >> ../conf/messages.en
echo "submitToHMRC.error.required = Select yes if submitToHMRC" >> ../conf/messages.en
echo "submitToHMRC.change.hidden = SubmitToHMRC" >> ../conf/messages.en

echo "Migration SubmitToHMRC completed"
