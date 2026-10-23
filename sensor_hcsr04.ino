#include <WiFi.h>
#include <WebServer.h>

// Access Point
const char* ssid     = "ESP32_ControlIoT";
const char* password = "Ev2.1234";   

const char* API_KEY = "ev2-sensor-2026";

WebServer server(80);

// HC-SR04
const int TRIG = 5;
const int ECHO = 18;  

const float UMBRAL_CM = 20.0;

// medicion distancia 
float medirUnaVez() {
  digitalWrite(TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG, LOW);

  long duracion = pulseIn(ECHO, HIGH, 30000);
  if (duracion == 0) {
    return -1;
  }
  return duracion * 0.0343 / 2.0;
}

// Promedio de 3 lecturas validas
float medirDistancia() {
  float suma = 0;
  int validas = 0;
  for (int i = 0; i < 3; i++) {
    float d = medirUnaVez();
    if (d > 0) {
      suma += d;
      validas++;
    }
    delay(30);
  }
  if (validas == 0) {
    return -1;
  }
  return suma / validas;
}

void paginaPrincipal() {
  server.send(200, "text/plain", "ESP32 HC-SR04 funcionando");
}

void leerSensor() {
  // solo responde si la clave es correcta
  if (server.header("X-API-Key") != API_KEY) {
    server.send(401, "text/plain", "NO_AUTORIZADO");
    return;
  }

  float distancia = medirDistancia();
  String estado;

  if (distancia < 0) {
    estado = "FUERA_RANGO";
  } else if (distancia <= UMBRAL_CM) {
    estado = "OBJETO_CERCA";
  } else {
    estado = "SIN_OBSTACULO";
  }

  // distancia, estado   
  server.send(200, "text/plain", String(distancia, 1) + ";" + estado);
}

void setup() {
  Serial.begin(115200);

  pinMode(TRIG, OUTPUT);
  pinMode(ECHO, INPUT);
  digitalWrite(TRIG, LOW);

  // red visible
  WiFi.softAP(ssid, password, 1, 0, 2);

  Serial.println();
  Serial.println("ESP32 + HC-SR04");
  Serial.print("IP del ESP32: ");
  Serial.println(WiFi.softAPIP());

  const char* cabeceras[] = {"X-API-Key"};
  server.collectHeaders(cabeceras, 1);

  server.on("/", paginaPrincipal);
  server.on("/sensor", leerSensor);
  server.begin();

  Serial.println("Servidor HTTP iniciado");
}

void loop() {
  server.handleClient();
}
