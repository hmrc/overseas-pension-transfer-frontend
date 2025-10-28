#!/bin/bash

echo ""
echo "Applying migration SubmittedVersionSummary"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /submittedVersionSummary                       controllers.SubmittedVersionSummaryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "submittedVersionSummary.title = submittedVersionSummary" >> ../conf/messages.en
echo "submittedVersionSummary.heading = submittedVersionSummary" >> ../conf/messages.en

echo "Migration SubmittedVersionSummary completed"
