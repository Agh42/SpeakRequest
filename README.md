# SpeakRequest

**A meeting manager for live and hybrid meetings**

[![Screenshot](/docs/images/landing.png)](https://speek.now)

In any meeting it is important that:

- the meeting goal is transparent to all participants (information sharing, decision making, brainstorming, etc.)
- how a decision will be made (consensus, majority vote, gradient of agreement, etc.)
- all participants get equal opportunities to speak
- it is transparent who is currently speaking
- it is transparent who is next in line to speak

SpeakRequest is a meeting manager designed for live and hybrid meetings. 

It helps [discussion moderators](https://en.wikipedia.org/wiki/Discussion_moderator) or [facilitators](https://en.wikipedia.org/wiki/Facilitator) in meetings and conferences by managing speak requests from participants, transparently showing who has the right to speak and for how long they've been talking. Each participant's place in the queue is visible at any moment.

A popout view can be placed by the facilitator on a projector or shared on-screen via web conferencing software such as Microsoft Teams, Zoom, or similar platforms. 

It also allows quick and hassle-free conduction of live polls with different poll types supported. Local participants can vote by scanning the QR code shown on the projector screen. Remote participants can easily join by entering the simple four-letter room code.

## 🚀 Access Live Instance

### Use it now at: **[https://speek.now](https://speek.now)**

## 📦 Run it yourself

SpeakRequest is published on Docker Hub and can be run anywhere with Docker:

```bash
docker run -p 8080:8080 agh42/speakrequest
```

Then open your browser to `http://localhost:8080` or your server's IP address/domain. SpeakRequest will honor the `X-Forwarded-*` headers, so it can be run behind a reverse proxy as well. All generated URLs and QR codes will point to the URL as seen by the client. You should be able to host it under any domain without further configuration.

## ✨ Features

- **Speak Request Queue**: Manage speaking turns with a transparent queue system
- **Speech Timer**: Track how long each participant has been speaking
- **Live Polls**: Conduct realtime polls with various poll types
- **QR Code Access**: Local participants can join via QR code
- **Easy Room Codes**: Simple four-letter codes for hassle-free joining
- **Hybrid Meeting Support**: Works seamlessly for both in-person and remote participants
- **Projector/Screen Share Ready**: Popout view optimized for projection and screen sharing
- **Real-time Updates**: WebSocket-based live updates for all participants

## 🛠️ Building from Source

### Prerequisites

- Java 21 or higher
- Gradle 8.9 (or use the included Gradle wrapper)

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Agh42/SpeakRequest.git
   cd SpeakRequest
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

   Or run the built JAR directly:
   ```bash
   java -jar build/libs/SpeakRequest-0.0.1-SNAPSHOT.jar
   ```

The application will start on `http://localhost:8080`

## 🐳 Building the Docker Image

```bash
docker build -t speakrequest .
docker run -p 8080:8080 speakrequest
```

## 📝 Configuration

The application can be configured via `application.yaml`. The default configuration supports:
- Forward headers for reverse proxy setups
- Remote IP header support
- Protocol header support

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0) - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- **Live Instance**: [https://speek.now](https://speek.now)
- **Docker Hub**: [agh42/speakrequest](https://hub.docker.com/r/agh42/speakrequest)
- **GitHub**: [Agh42/SpeakRequest](https://github.com/Agh42/SpeakRequest)



