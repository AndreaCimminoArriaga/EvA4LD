# 1 - Quickstart

Para lanzar un experimento basta ejecutar lo siguiente:

> java -jar scindere.jar experiments/environments/restaurants.cnf experiments/algorithms/moea-cimmino.cnf 0

El primer argumento es un fichero de configuración que especifica los detalles de una tarea de **enlazado**. El segundo argumento indica que **algoritmo genético** se desea compilar usando el framework. Por último, se especifica que **número de setup** se desea utilizar junto con el algoritmo

## 1.1 Configurar una tarea de enlazado

Para configurar una tarea de **enlazado** hay que crear un fichero localizado en la carpeta *experiments/environments/* con la extensión *.cnf* (por ejemplo *restaurants.cnf*). El contenido del fichero es bastante intuitivo, para explicarlo veamos el siguiente ejemplo:
```
name				  := restaurants
source_dataset 		  := ./tdb-data/restaurants1
target_dataset 		  := ./tdb-data/restaurants2

specifications_output := ./experiments/restaurants_specifications.csv
links_output   		  := ./experiments/output_links/restaurants_output.nt

examples_file  		  := ./experiments/5p-samples/empty.nt
gold_standard 		  := ./experiments/gold-stds/restaurant1-restaurant2-gold.nt 

algorithm_statistics_file := ./experiments/statistics.csv

source_class_restrictions := http://www.okkam.org/ontology_restaurant1.owl#Restaurant
target_class_restrictions := http://www.okkam.org/ontology_restaurant2.owl#Restaurant

suitable_attributes := http://www.okkam.org/ontology_restaurant1.owl#name, http://www.okkam.org/ontology_restaurant2.owl#name
suitable_attributes := http://www.okkam.org/ontology_restaurant1.owl#phone_number, http://www.okkam.org/ontology_restaurant2.owl#phone_number
```
En primer lugar se especifica el nombre de la tarea de enlazado,  en este caso:
> **name :=** restaurants 

Después hay que indicar dónde se encuentran los dos datasets a enlazar. Es recomendable usar siempre la misma estructura de proyecto para facilitar que otros usuarios puedan lanzar fácilmente nuestros experimentos u otros nuevos: 
> **source_dataset :=** ./tdb-data/restaurants1
> **target_dataset :=** ./tdb-data/restaurants2

A continuación hay que indicar en que directorio se van a guardar las reglas que se generen, así como los enlaces generados por dichas reglas, y el fichero donde serán almacenadas las estadísticas de la ejecución. **Sin embargo, estas funcionalidades se encuentra desactivadas en la versión actual (igualmente hay que indicarlo todo para que el framework funcione correctamente)**.

> **specifications_output :=** ./experiments/restaurants_specifications.csv 
> **links_output :=** ./experiments/output_links/restaurants_output.nt
>**algorithm_statistics_file :=** ./experiments/statistics.csv 

Lo siguiente a indicar es de dónde se van a extraer los ejemplos que se usarán para el aprendizaje y el fichero conteniendo el gold standard de esta tarea de enlazado. El fichero de ejemplos por el momento tiene que apuntar a un fichero vacío, en la sección **Scinedere with provided examples** explicamos como ejecutar el programa usando el conjunto de ejemplos contenido en dicho fichero.
>**examples_file :=**./experiments/5p-samples/empty.nt 
>**gold_standard :=**./experiments/gold-stds/restaurant1-restaurant2-gold.nt 

Finalmente, tenemos que indicar las clases de las instancias que vamos a enlazar, tanto en el dataset source como en el target, así como los pares de atributos que deseamos que los algoritmos genéticos tengan en cuenta a la hora de generar las reglas. 
>**source_class_restrictions :=** http://www.okkam.org/ontology_restaurant1.owl#Restaurant 
>**target_class_restrictions :=** http://www.okkam.org/ontology_restaurant2.owl#Restaurant 
>**suitable_attributes :=** http://www.okkam.org/ontology_restaurant1.owl#name, http://www.okkam.org/ontology_restaurant2.owl#name 
>**suitable_attributes :=** http://www.okkam.org/ontology_restaurant1.owl#phone_number, http://www.okkam.org/ontology_restaurant2.owl#phone_number

Nótese la siguiente restricción, los datsets sólo se pueden filtrar por una clase. Por lo tanto si quisiéramos enlazar las instancias de la clase *sch:Person* no habría problema, pero si fuera por las clases *sch:Person* y *sch:Actor* deberíamos de escoger una sola. 

Por último, se pueden añadir tantos pares de atributos como se desee siguiendo el patrón del ejemplo:
>**suitable_attributes :=** [atributo source], [atributo target]

## 1.2 Definir un algoritmo genético
Cuando se define un algoritmo genético, lo que realmente se está haciendo es indicar al framework las características del algoritmo que deseamos usar (y que el framework creará on-the-fly). Para ello, debemos crear un fichero en el directorio *experiments/algorithms/* cuya extensión debe ser igual que antes *.cnf*, por ejemplo *carvalho.cnf*. Para explicar el contenido de este fichero vamos a basarnos en el siguiente ejemplo:

````
#> -- Algorithm specification --

name := moea-carvalho-builded

#>  Algorithm elements

initializer_class := tdg.link_discovery.connector.sparql.algorithm.initializer.TreeGrowCreator
selector_class := tdg.link_discovery.middleware.moea.algorithm.selector.RouletteWheelSelector
crossover_class := tdg.link_discovery.middleware.moea.genetics.variation.crossovers.SubtreeCrossover
mutation_class := tdg.link_discovery.middleware.moea.genetics.variation.mutations.TreeMutation
fitness_class := tdg.link_discovery.middleware.moea.genetics.fitness_function.FMeasureFitness
replacement_class := tdg.link_discovery.middleware.framework.algorithm.replacement.RandomReplacement

#> Learner elements

#attribute_learner_class := none
engine_class := tdg.link_discovery.connector.sparql.engine.SparqlEngine

#> Attribute value restrictions elements

string_metrics_classes := tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.LevenshteinSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.CosineSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroWinklerTFIDFSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.SoftTFIDFSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroWinklerSimilarity
transformation_classes := tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.LowercaseTransformation,tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.TokenizeTransformation,tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.StripUriPrefixTransformation
aggregate_classes := tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Max,tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Min,tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg
````

Lo primero a indicar es el nombre del algoritmo:
>**name :=** moea-carvalho-builded

A continuación especificamos los operadores genéticos que deseamos que posea:
* Con que operador deseamos que se cree la primera tanda de reglas que conforman la población:
>**initializer_class :=** tdg.link_discovery.connector.sparql.algorithm.initializer.TreeGrowCreator

* Con que operador deseamos que en cada iteración se escojan las reglas que van a ser cruzadas y mutadas, así como dichos operadores:
>**selector_class :=** tdg.link_discovery.middleware.moea.algorithm.selector.RouletteWheelSelector
>**crossover_class :=** tdg.link_discovery.middleware.moea.genetics.variation.crossovers.SubtreeCrossover
>**mutation_class :=** tdg.link_discovery.middleware.moea.genetics.variation.mutations.TreeMutation
* La función objetivo que debe seguir el algoritmo:
>**fitness_class :=** tdg.link_discovery.middleware.moea.genetics.fitness_function.FMeasureFitness
* El operador con el que se combinará la población de una iteración con la población nueva generada a partir de las operaciones de selección, cruce, y mutación.
>**replacement_class :=** tdg.link_discovery.middleware.framework.algorithm.replacement.RandomReplacement

A continuación se indica que algoritmo queremos usar para aprender automáticamente que pares de atributos son útiles para la tarea de enlazado. Si se indica un algoritmo para aprender automáticamente pares de atributos, entonces en el fichero de la **tarea de enlazado** NO DEBE APARECER NINGÚN PAR DE ATRIBUTOS. En nuestro ejemplo nosotros hemos comentado esta línea. Adicionalmente hay que indicar que motor de enlazado vamos a querer usar, actualmente solo usamos *SparqlEngine* por lo que aquí no hay que cambiar nada:
 
>#**attribute_learner_class :=** none 
>**engine_class :=** tdg.link_discovery.connector.sparql.engine.SparqlEngine

Por último, tenemos que indicar que **funciones de string** vamos a permitir que el algoritmo genético use, así como las **funciones de agregación** y **funciones de transformación**

>**string_metrics_classes :=** tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.LevenshteinSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.CosineSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroWinklerTFIDFSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.SoftTFIDFSimilarity,tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.JaroWinklerSimilarity

>**transformation_classes :=** tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.LowercaseTransformation,tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.TokenizeTransformation,tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.StripUriPrefixTransformation

>**aggregate_classes :=** tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Max,tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Min,tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg


En caso de querer cambiar cualquiera de los parámetros anteriores, basta con consultar la ruta usada por el parámetro que queremos cambiar. Por ejemplo la ruta de **tdg.link_discovery.middleware.framework.algorithm.replacement.RandomReplacement** sería *tdg/link_discovery/middleware/framework/algorithm/replacement/*, en dicho directorio encontraremos qué otras opciones se pueden usar en lugar de, siguiendo con el ejemplo, **RandomReplacement**.

## 1.3 Definiendo un fichero de setup
Un fichero de setup contiene una serie de parámetros numéricos requeridos por el algoritmo genético que el framework compilará. El fichero de setup debe colocarse en el mismo directorio que el fichero del algoritmo, es decir *experiments/algorithms/*, y el NOMBRE DEL FICHERO TIENE QUE SEGUIR EL SIGUIENTE PATRON *[nombre_del_algoritmo]_setup[número_del_setup].cnf* por ejemplo *carvalho_setup4.cnf*. Para explicar el contenido del fichero vamos a usar el siguiente ejemplo:
````
max_iterations := 50
population_size := 20
mutation_rate := 0.5
variables_num := 1
selector_arity := 2
crossover_rate := 0.25
objectives_num := 1
generations_rate := 10
````
* **max_iterations** indica el número de iteraciones máximas que podrá dar el algoritmo.
* **population_size** indica el tamaño de la población de reglas, es decir, el número de reglas que se generarán durante el aprendizaje y que se evolucionarán hasta encontrar una solución o alcanzar un criterio de parada.
* **mutation_rate** indica la probabilidad de que ocurra una mutación.
* **crossover_rate** indica la probabilidad con la que un cruce puede ocurrir.
* **variables_num** indica el número de variables que usará el algoritmo genético en cada cromosoma, en este caso siempre es una regla, por lo que el valor es siempre *1*.
* **selector_arity** indica el número de reglas que el operador de selección escogerá de la población en cada iteración.
* **objectives_num** indica el número de funciones objetivo que usará el algoritmo, este parámetro de momento debe permanecer siempre constante a *1*.
* **generations_rate** indica un número de iteraciones durante las cuales, si el fitness de la población no cambia el algoritmo parará. Es decir, si este parámetro es *10*, significa que si este algoritmo itera 10 veces y el fitness de todas las reglas de la población permanece sin variar; entonces el algoritmo parará.

## 1.3 Scinedere with provided examples