Repositorio de la práctica (guía completa): https://github.com/alefq/afq-taller-git-2024 (docs/TALLER_GIT.md).

Objetivo. 
Publicar un servicio HTTP propio (Spring Boot, API REST) que versiona el modelado de las clases del 2 y 3 de septiembre (Minecraft o Counter-Strike 2), con herencia, sobreescritura y ocultamiento de la información.

Consignas. 
(1) Crear en GitHub el repositorio público INICIALES-taller-git-2026 con README y Apache License 2.0; clonar por HTTPS. 
(2) Incorporar un proyecto Spring Boot desde start.spring.io (Maven, Java 21, Spring Web) y verificar ./mvnw spring-boot:run. 
(3) Copiar las clases del modelado, corregir compilación, add / commit / push. 
(4) IndexController (GET /) y un controller que construya una instancia del dominio con parámetros de la URL. 
(5) Método abstracto en la clase base, dos hijas independientes, JSON con el comportamiento de cada una (el controller usa el tipo padre). 
(6) El README incluye el diagrama Mermaid de esas clases.

Entrega en Classroom. 
  Adjunto un archivo Markdown (.md), PDF o Word (.docx) con estas especificaciones aplicadas a su dominio (no un transcript de chat, hacerlo en forma de biácora).
 El código queda en GitHub. Incluir en la entrega el enlace al repositorio.


Ejercicio · opción A
Minecraft — endurecer el modelo



Partan del diagrama y del código de POO-03. No agreguen atributos «porque sí».

El modelo debe permitir tratar cualquier entidad de forma uniforme (moverse, recibir daño, desaparecer) sin preguntar de qué tipo concreto es.

Las reglas de vida, movimiento y muerte viven en la jerarquía: la clase padre concentra lo común; cada subclase solo redefine lo que realmente cambia (un Creeper no se comporta como un Aldeano).

Nadie fuera de la jerarquía debe poder dejar una entidad en un estado imposible. Si un campo es protected, justifiquen por qué la hija lo necesita y un método no alcanza.

Ejercicio · opción B
Counter-Strike 2 — endurecer el modelo



Partan del diagrama y del código de POO-03.

El inventario de un jugador debe poder contener cualquier arma y pedirle que dispare, recargue o se muestre en la tienda, sin un if por cada tipo.

El cálculo de daño, el control de munición y el cooldown (granadas) no se pisan desde afuera. La clase padre concentra el contrato; Rifle, Pistola, Sniper y Granada especializan el comportamiento.

Si dos armas copian el mismo método, falta generalización. Si una hija abre todos los campos a public, falta ocultamiento.