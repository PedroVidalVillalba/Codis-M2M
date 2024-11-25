# Instrucciones para el uso correcto del servidor.
## Generación de claves.
Para poder autenticar al servidor desde los usuarios, es necesario generar un par de claves pública y privada del servidor.
Ejecutar para ello, desde el directorio `.../m2m/server/src/resources`, el script:
```shell
./generate_server_keys.sh
```

## Base de datos.
Para generar la base de datos con un usuario nuevo, una nueva base de datos y las tablas actualizadas,
será necesario ejecutar el script `.../m2m/server/src/resources/setup_database.sql`.
Para ello, será necesario un usuario con permisos suficientes de creación de usuarios, 
por lo que posiblemente se necesiten permisos de administrador. 

En el directorio anterior, ejecutar desde un terminal:
```shell
sudo -i -u postgres psql -f setup_database.sql
```
Alternativamente, si se dispone de la contraseña del usuario de postgres, se puede ejecutar:
```shell
su postgres -c "psql -f setup_database.sql"
```
Este script es idempotente, así que no supone riesgo ejecutarlo múltiples veces.

### Configuración de IntelliJ
A la derecha hay una pestaña con bases de datos. Clic en el +, Data source, PostreSQL
Host: localhost, port:5432, user: m2m, password: m2m, database: m2mdb.
Test connection para verificar que está todo bien

### Problemas con contraseñas en postgres
Si al ejecutar se observa algún problema relacionado con el uso de contraseña en postgres, 
se puede modificar el servicio para permitir este tipo de autenticación.

Se debe localizar el fichero `pg_hba.conf` de configuración de postgres. Una vez localizado, modificar la línea
que hace referencia a las conexiones locales, y dejarla como aparece abajo:
```textmate
# "local" is for Unix domain socket connections only
local   all             all                                     md5
```
En Arch Linux, por lo menos, funciona también con la configuración:
```textmate
# "local" is for Unix domain socket connections only
local   all             all                                     trust
```

Si se modifica este archivo, será necesario reiniciar postgres:
```shell
sudo systemctl restart postgres
```