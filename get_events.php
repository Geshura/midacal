<?php
$conn = new mysqli("localhost", "root", "", "kalendarz");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$start = $_GET['start']; 
$end = $_GET['end'];     

$sql = "SELECT * FROM events WHERE start_datetime BETWEEN ? AND ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $start, $end);
$stmt->execute();
$result = $stmt->get_result();

$events = [];
while($row = $result->fetch_assoc()) {
    $events[] = $row;
}

echo json_encode($events);
?>
