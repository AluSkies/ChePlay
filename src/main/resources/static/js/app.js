async function createSample(){
  await fetch('/api/graph/sample', {method:'POST'});
  document.getElementById('output').innerText = 'Sample graph created';
}
async function runBFS(){
  const res = await fetch('/api/algorithms/bfs', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body: JSON.stringify({start:'A'})
  });
  const json = await res.json();
  document.getElementById('output').innerText = JSON.stringify(json, null, 2);
}
async function runDijkstra(){
  const res = await fetch('/api/algorithms/dijkstra', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body: JSON.stringify({start:'A'})
  });
  const json = await res.json();
  document.getElementById('output').innerText = JSON.stringify(json, null, 2);
}