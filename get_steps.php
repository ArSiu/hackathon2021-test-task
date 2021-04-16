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

$user_id = $_GET['id'];

$date = date("Y-m-d");  

$sql = "SELECT SUM(steps) as sum FROM steps WHERE id= $user_id AND data LIKE '$date %'";

$result = $conn->query($sql);

echo json_encode(array('sum' => $result->fetch_row()[0]));

?>
