<?php
$conn = new mysqli("localhost", "root", "", "kalendarz");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$title = $_POST['title'];
$description = $_POST['description'];
$start = $_POST['start_datetime'];
$end = $_POST['end_datetime'];

$sql = "INSERT INTO events (title, description, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ssss", $title, $description, $start, $end);
$stmt->execute();

echo json_encode(["status" => "success"]);
?>
