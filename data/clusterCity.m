function clusterCity(M, a)
	length = size(M,1);
	% disp(length)
	nd = nonzeros(M);
	nd = nd';
	z = linkage(nd);
	dendrogram(z,length);

	T = cluster(z,'maxclust', a);
	for i = 1:a
		tm = find(T == i);
		% tm = reshape(tm, 1, length(tm));
		% disp(tm);
		tm = tm';
		fprintf('%d, %s\n', i, int2str(tm));
	end
end 
