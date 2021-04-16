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
$is_google = $_GET['isgoogle'];


$sql = "INSERT IGNORE INTO users (user_id, is_google) VALUES ($user_id, $is_google)";

if ($conn->query($sql) === TRUE) {
    $sql = "SELECT id FROM users WHERE user_id = $user_id AND is_google=$is_google";
    $result = $conn->query($sql);
    
    echo json_encode(array('id' => $result->fetch_row()[0]));
} else {
    echo json_encode(array('error' => 0));}

?>
