#!/bin/bash

echo ""
echo "Applying migration MemberIsResident"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /memberIsResident                        controllers.MemberIsResidentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /memberIsResident                        controllers.MemberIsResidentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeMemberIsResident                  controllers.MemberIsResidentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeMemberIsResident                  controllers.MemberIsResidentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "memberIsResident.title = memberIsResident" >> ../conf/messages.en
echo "memberIsResident.heading = memberIsResident" >> ../conf/messages.en
echo "memberIsResident.yes = Yes" >> ../conf/messages.en
echo "memberIsResident.no = No" >> ../conf/messages.en
echo "memberIsResident.checkYourAnswersLabel = memberIsResident" >> ../conf/messages.en
echo "memberIsResident.error.required = Select memberIsResident" >> ../conf/messages.en
echo "memberIsResident.change.hidden = MemberIsResident" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryMemberIsResident: Arbitrary[MemberIsResident] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(MemberIsResident.values.toSeq)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Migration MemberIsResident completed"
