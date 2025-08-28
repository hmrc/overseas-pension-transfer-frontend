#!/bin/bash

echo ""
echo "Applying migration QROPSOtherCountry"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /qROPSOtherCountry                        controllers.QROPSOtherCountryController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /qROPSOtherCountry                        controllers.QROPSOtherCountryController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeQROPSOtherCountry                  controllers.QROPSOtherCountryController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeQROPSOtherCountry                  controllers.QROPSOtherCountryController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "qROPSOtherCountry.title = qROPSOtherCountry" >> ../conf/messages.en
echo "qROPSOtherCountry.heading = qROPSOtherCountry" >> ../conf/messages.en
echo "qROPSOtherCountry.checkYourAnswersLabel = qROPSOtherCountry" >> ../conf/messages.en
echo "qROPSOtherCountry.error.required = Enter qROPSOtherCountry" >> ../conf/messages.en
echo "qROPSOtherCountry.error.length = QROPSOtherCountry must be 35 characters or less" >> ../conf/messages.en
echo "qROPSOtherCountry.change.hidden = QROPSOtherCountry" >> ../conf/messages.en

echo "Migration QROPSOtherCountry completed"
