#!/bin/bash

echo ""
echo "Applying migration MemberIsResidentUK"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /memberIsResidentUK                        controllers.MemberIsResidentUKController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /memberIsResidentUK                        controllers.MemberIsResidentUKController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeMemberIsResidentUK                  controllers.MemberIsResidentUKController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeMemberIsResidentUK                  controllers.MemberIsResidentUKController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "memberIsResidentUK.title = memberIsResidentUK" >> ../conf/messages.en
echo "memberIsResidentUK.heading = memberIsResidentUK" >> ../conf/messages.en
echo "memberIsResidentUK.checkYourAnswersLabel = memberIsResidentUK" >> ../conf/messages.en
echo "memberIsResidentUK.error.required = Select yes if memberIsResidentUK" >> ../conf/messages.en
echo "memberIsResidentUK.change.hidden = MemberIsResidentUK" >> ../conf/messages.en

echo "Migration MemberIsResidentUK completed"
