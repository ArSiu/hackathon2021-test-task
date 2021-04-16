<?php

header('Content-Type: application/json');

$servername = "localhost";
$username = "id16621464_datab";
$password = "6K2wX4yEj86v0tp#";
$dbname = "id16621464_data";

$conn = new mysqli($servername, $username, $password,$dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(array('error' => 0));
}

$data = json_decode(file_get_contents('php://input'), true);

$user_id = $_GET["id"];
$is_google = $_GET["steps"];


$sql = "INSERT INTO steps (id, steps) VALUES ($user_id, $is_google)";

if ($conn->query($sql) === TRUE) {
    echo json_encode(array('error' => 1));
} else {
    echo json_encode(array('error' => 0));}

?>
