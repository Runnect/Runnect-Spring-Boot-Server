apiVersion: 1

contactPoints:
  - orgId: 1
    name: slack-alert
    receivers:
      - uid: slack-alert-receiver
        type: slack
        settings:
          url: "__SLACK_WEBHOOK_URL__"
          username: "Grafana"
